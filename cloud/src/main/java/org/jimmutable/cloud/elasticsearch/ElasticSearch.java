package org.jimmutable.cloud.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jimmutable.cloud.servlet_utils.common_objects.JSONServletResponse;
import org.jimmutable.cloud.servlet_utils.search.OneSearchResult;
import org.jimmutable.cloud.servlet_utils.search.SearchResponseError;
import org.jimmutable.cloud.servlet_utils.search.SearchResponseOK;
import org.jimmutable.cloud.servlet_utils.search.StandardSearchRequest;
import org.jimmutable.core.serialization.FieldName;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Use this class for general searching and document upserts with Elasticsearch
 * 
 * @author trevorbox
 *
 */

public class ElasticSearch implements ISearch
{

	private static final Logger logger = LogManager.getLogger(ElasticSearch.class);

	private static final ExecutorService document_upsert_pool = (ExecutorService) new ThreadPoolExecutor(8, 8, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	private static final String ELASTICSEARCH_DEFAULT_TYPE = "default";

	private volatile TransportClient client;

	public ElasticSearch(TransportClient client)
	{
		this.client = client;
	}

	/**
	 * Gracefully shutdown the running threads. Note: the TransportClient should be
	 * closed where instantiated. This is not handles by this.
	 * 
	 * @return boolean if shutdown correctly or not
	 */
	@Override
	public boolean shutdownDocumentUpsertThreadPool(int seconds)
	{

		long start = System.currentTimeMillis();

		document_upsert_pool.shutdown();

		boolean terminated = true;

		try
		{
			terminated = document_upsert_pool.awaitTermination(seconds, TimeUnit.SECONDS);
		} catch (InterruptedException e)
		{
			logger.log(Level.FATAL, "Shutdown of runnable pool was interrupted!", e);
		}

		if (!terminated)
		{
			logger.error("Failed to terminate in %s seconds. Calling shutdownNow...", seconds);
			document_upsert_pool.shutdownNow();
		}

		boolean success = document_upsert_pool.isTerminated();

		if (success)
		{
			logger.warn(String.format("Successfully terminated pool in %s milliseconds", (System.currentTimeMillis() - start)));
		} else
		{
			logger.warn(String.format("Unsuccessful termination of pool in %s milliseconds", (System.currentTimeMillis() - start)));
		}
		return success;
	}

	/**
	 * Runnable class to upsert the document
	 * 
	 * @author trevorbox
	 *
	 */
	private class UpsertDocumentRunnable implements Runnable
	{
		private Indexable object;
		private Map<String, Object> data;

		public UpsertDocumentRunnable(Indexable object, Map<String, Object> data)
		{
			this.object = object;
			this.data = data;
		}

		@Override
		public void run()
		{
			try
			{
				String index_name = object.getSimpleSearchIndexDefinition().getSimpleValue();
				String document_name = object.getSimpleSearchDocumentId().getSimpleValue();
				IndexResponse response = client.prepareIndex(index_name, ELASTICSEARCH_DEFAULT_TYPE, document_name).setSource(data).get();

				Level level;
				switch (response.getResult())
				{
				case CREATED:
					level = Level.INFO;
					break;
				case UPDATED:
					level = Level.INFO;
					break;
				default:
					level = Level.FATAL;
					break;
				}

				logger.log(level, String.format("%s %s/%s/%s %s", response.getResult().name(), index_name, ELASTICSEARCH_DEFAULT_TYPE, document_name, data));

			} catch (Exception e)
			{
				logger.log(Level.FATAL, "Failure during upsert operation!", e);
			}
		}
	}

	/**
	 * Upsert a document to a search index
	 * 
	 * @param object
	 *            The Indexable object
	 * @return boolean
	 * @throws InterruptedException
	 */
	@Override
	public boolean upsertDocumentAsync(Indexable object)
	{

		if (object == null)
		{
			logger.error("Null object!");
			return false;
		}

		SearchDocumentWriter writer = new SearchDocumentWriter();
		object.writeSearchDocument(writer);
		Map<String, Object> data = writer.getSimpleFieldsMap();

		try
		{
			document_upsert_pool.execute(new UpsertDocumentRunnable(object, data));
		} catch (Exception e)
		{
			logger.log(Level.FATAL, "Failure during thread pool execution!", e);
			return false;
		}

		return true;
	}

	/**
	 * Search an index with a query string.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">query-dsl-query-string-query</a>
	 * 
	 * @param index
	 * @param request
	 * @return JSONServletResponse
	 */
	@Override
	public JSONServletResponse search(IndexDefinition index, StandardSearchRequest request)
	{

		if (index == null || request == null)
		{
			return new SearchResponseError(request, "Null parameter(s)!");
		}

		try
		{

			String index_name = index.getSimpleValue();
			int from = request.getSimpleStartResultsAfter();
			int size = request.getSimpleMaxResults();

			SearchRequestBuilder builder = client.prepareSearch(index_name);
			builder.setTypes(ELASTICSEARCH_DEFAULT_TYPE);
			builder.setFrom(from);
			builder.setSize(size);
			builder.setQuery(QueryBuilders.queryStringQuery(request.getSimpleQueryString()));

			SearchResponse response = builder.get();

			List<OneSearchResult> results = new LinkedList<OneSearchResult>();

			response.getHits().forEach(hit ->
			{
				Map<FieldName, String> map = new HashMap<FieldName, String>();
				hit.getSourceAsMap().forEach((k, v) ->
				{
					map.put(new FieldName(k), v.toString());
				});
				results.add(new OneSearchResult(map));
			});

			int next_page = from + size;

			// if the size was met try and see if there are more results

			logger.info(String.format("TOTAL:%s SIZE:%s", response.getHits().totalHits, response.getHits().getHits().length));

			boolean has_more_results = response.getHits().totalHits > next_page;

			boolean has_previous_results = from != 0;

			Level level;
			switch (response.status())
			{
			case OK:
				level = Level.INFO;
				break;
			default:
				level = Level.WARN;
				break;
			}

			SearchResponseOK ok = new SearchResponseOK(request, results, from, has_more_results, has_previous_results, next_page, from);

			logger.log(level, String.format("Status:%s Hits:%s TotalHits:%s StandardSearchRequest:%s first_result_idx:%s has_more_results:%s has_previous_results:%s start_of_next_page_of_results:%s start_of_previous_page_of_results:%s", response.status(), results.size(), response.getHits().totalHits, ok.getSimpleSearchRequest(), ok.getSimpleFirstResultIdx(), ok.getSimpleHasMoreResults(), ok.getSimpleHasMoreResults(), ok.getSimpleStartOfNextPageOfResults(), ok.getSimpleStartOfPreviousPageOfResults()));
			logger.trace(ok.getSimpleResults().toString());

			return ok;

		} catch (Exception e)
		{
			logger.warn(String.format("Search failed for %s", request), e);
			return new SearchResponseError(request, e.getMessage());
		}

	}

	/**
	 * Test if the index exists or not
	 * 
	 * @param index
	 *            IndexDefinition
	 * @return boolean if the index exists or not
	 */
	@Override
	public boolean indexExists(IndexDefinition index)
	{
		if (index == null)
		{
			logger.fatal("Cannot check the existence of a null Index");
			return false;
		}
		try
		{
			return client.admin().indices().prepareExists(index.getSimpleValue()).get().isExists();
		} catch (Exception e)
		{
			logger.log(Level.FATAL, "Failed to check if index exists", e);
			return false;
		}
	}

	/**
	 * Test if the index exists or not
	 * 
	 * @param index
	 *            SearchIndexDefinition
	 * @return boolean if the index exists or not
	 */
	@Override
	public boolean indexExists(SearchIndexDefinition index)
	{
		if (index == null)
		{
			logger.fatal("Cannot check the existence of a null Index");
			return false;
		}
		try
		{
			return client.admin().indices().prepareExists(index.getSimpleIndex().getSimpleValue()).get().isExists();
		} catch (Exception e)
		{
			logger.log(Level.FATAL, "Failed to check if index exists", e);
			return false;
		}
	}

	/**
	 * An index is properly configured if it exists and its field names and
	 * datatypes match
	 * 
	 * @param index
	 *            SearchIndexDefinition
	 * @return boolean if the index is properly configured or not
	 */
	@Override
	public boolean indexProperlyConfigured(SearchIndexDefinition index)
	{

		if (index == null)
		{
			return false;
		}

		if (indexExists(index))
		{

			// compare the expected index fields to the actual index fields
			Map<String, String> expected = new HashMap<String, String>();
			index.getSimpleFields().forEach(fields ->
			{
				expected.put(fields.getSimpleFieldName().getSimpleName(), fields.getSimpleType().getSimpleCode());
			});

			try
			{
				GetMappingsResponse response = client.admin().indices().prepareGetMappings(index.getSimpleIndex().getSimpleValue()).get();

				String json = response.getMappings().get(index.getSimpleIndex().getSimpleValue()).get(ELASTICSEARCH_DEFAULT_TYPE).source().string();

				Map<String, String> actual = new HashMap<String, String>();

				new ObjectMapper().readTree(json).get(ELASTICSEARCH_DEFAULT_TYPE).get("properties").fields().forEachRemaining(fieldMapping ->
				{
					actual.put(fieldMapping.getKey(), fieldMapping.getValue().get("type").asText());
				});

				if (!expected.equals(actual))
				{

					logger.info("Index not properly configured");
					logger.info(expected);
					logger.info(actual);

					return false;

				}

				return true;

			} catch (Exception e)
			{
				logger.log(Level.FATAL, String.format("Failed to get the index mapping for index %s", index.getSimpleIndex().getSimpleValue()), e);
			}
		}

		return false;

	}

	private boolean createIndex(SearchIndexDefinition index)
	{
		if (index == null)
		{
			logger.fatal("Cannot create a null Index");
			return false;
		}

		try
		{

			XContentBuilder mappingBuilder = jsonBuilder();
			mappingBuilder.startObject().startObject(ELASTICSEARCH_DEFAULT_TYPE).startObject("properties");
			for (SearchIndexFieldDefinition field : index.getSimpleFields())
			{
				// if (field.getSimpleType().equals(SearchIndexFieldType.OBJECTID))
				// {
				// // EXPLICIT MAPPING FOR OBJECTID - does not rely on enum's simple code
				// // https://www.elastic.co/blog/strings-are-dead-long-live-strings
				// mappingBuilder.startObject(field.getSimpleFieldName().getSimpleName());
				// /* */mappingBuilder.field("type", "text");
				// /* */mappingBuilder.startObject("fields");
				// /* */mappingBuilder.startObject("keyword");
				// /* */mappingBuilder.field("type", "keyword");
				// /* */mappingBuilder.field("ignore_above", 256);
				// /* */mappingBuilder.endObject();
				// /* */mappingBuilder.endObject();
				// mappingBuilder.endObject();
				// } else
				// {
				mappingBuilder.startObject(field.getSimpleFieldName().getSimpleName());
				/*	*/mappingBuilder.field("type", field.getSimpleType().getSimpleCode());
				mappingBuilder.endObject();
				// }
			}
			mappingBuilder.endObject().endObject().endObject();

			CreateIndexResponse createResponse = client.admin().indices().prepareCreate(index.getSimpleIndex().getSimpleValue()).addMapping(ELASTICSEARCH_DEFAULT_TYPE, mappingBuilder).get();

			if (!createResponse.isAcknowledged())
			{
				logger.fatal(String.format("Index Creation not acknowledged for index %s", index.getSimpleIndex().getSimpleValue()));
				return false;
			}

		} catch (Exception e)
		{
			logger.log(Level.FATAL, String.format("Failed to generate mapping json for index %s", index.getSimpleIndex().getSimpleValue()), e);
			return false;
		}

		logger.info(String.format("Created index %s", index.getSimpleIndex().getSimpleValue()));
		return true;
	}

	private boolean deleteIndex(SearchIndexDefinition index)
	{
		if (index == null)
		{
			logger.fatal("Cannot delete a null Index");
			return false;
		}

		try
		{
			DeleteIndexResponse deleteResponse = client.admin().indices().prepareDelete(index.getSimpleIndex().getSimpleValue()).get();
			if (!deleteResponse.isAcknowledged())
			{
				logger.fatal(String.format("Index Deletion not acknowledged for index %s", index.getSimpleIndex().getSimpleValue()));
				return false;
			}

		} catch (Exception e)
		{
			logger.fatal(String.format("Index Deletion failed for index %s", index.getSimpleIndex().getSimpleValue()));
			return false;
		}
		logger.info(String.format("Deleted index %s", index.getSimpleIndex().getSimpleValue()));
		return true;

	}

	/**
	 * Upsert if the index doesn't exist or is not properly configured already
	 * 
	 * BE CAREFUL!!!
	 * 
	 * @param index
	 *            SearchIndexDefinition
	 * @return boolean if the upsert was successful or not
	 */
	@Override
	public boolean upsertIndex(SearchIndexDefinition index)
	{

		if (index == null)
		{
			logger.fatal("Cannot upsert a null Index");
			return false;
		}

		// if it exists and is not configured correctly delete and add
		if (indexExists(index))
		{
			if (!indexProperlyConfigured(index))
			{
				if (deleteIndex(index))
				{
					return createIndex(index);
				} else
				{
					// deletion failed
					return false;
				}
			}
		} else
		{
			// index is new
			return createIndex(index);
		}

		// index exists and already configured correctly
		logger.info(String.format("No upsert needed for index %s", index.getSimpleIndex().getSimpleValue()));
		return true;
	}

}