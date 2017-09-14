package org.jimmutable.cloud.elasticsearch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Utility class for search Index maintenance. Should be used on startup of
 * application that cares about properly configured search indices.
 * 
 * 
 * @author trevorbox
 *
 */

// TODO we need to come to a consensus on how to manage everything. Creating a
// single client takes 16 seconds so it would be better to simply have a
// centrally managed solution separated from this class i think

// CODE REVEIW: Generally, I think the lifecycle management (opening and closing
// clients) needs to be managed *for* the user. i.e. its bad that if you forget
// to call closeClient resources are not freed etc. I suggest making the
// constructor private and having public static methods to perform the logical
// operations needed for others...

public class SearchIndexConfigurationUtils
{

	private static final Logger logger = Logger.getLogger(SearchIndexConfigurationUtils.class.getName());

	private static TransportClient client;

	private static final String ELASTICSEARCH_DEFAULT_TYPE = "default";

	@SuppressWarnings("resource")
	public SearchIndexConfigurationUtils(ElasticSearchEndpoint endpoint)
	{
		// set cluster name?
		Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();

		try {

			// this is expensive - could take 16 seconds
			client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(endpoint.getSimpleHost()), endpoint.getSimplePort()));

		} catch (UnknownHostException e) {
			String errorMessage = String.format("Failed to create a TransportClient from endpoint %s:%d", endpoint.getSimpleHost(), endpoint.getSimplePort());
			logger.log(Level.SEVERE, errorMessage, e);
			throw new RuntimeException(errorMessage);
		}
	}

	// public static boolean
	// checkAllIndicesConfiguredCorrectly(ElasticSearchEndpoint endpoint,
	// List<SearchIndexDefinition> indices)
	// {
	// SearchIndexConfigurationUtils util = new
	// SearchIndexConfigurationUtils(endpoint);
	//
	// boolean status = true;
	// for (SearchIndexDefinition index : indices) {
	// if (!util.indexProperlyConfigured(index)) {
	// status = false;
	// }
	// }
	// util.closeClient();
	// return status;
	// }
	//
	// public static boolean upsertAll(ElasticSearchEndpoint endpoint,
	// List<SearchIndexDefinition> indices)
	// {
	// SearchIndexConfigurationUtils util = new
	// SearchIndexConfigurationUtils(endpoint);
	// boolean status = true;
	// for (SearchIndexDefinition index : indices) {
	// if (!util.upsertIndex(index)) {
	// status = false;
	// }
	// }
	// util.closeClient();
	// return status;
	// }

	/**
	 * 
	 * @param index
	 *            IndexDefinition
	 * @return boolean if the index exists or not
	 */
	public boolean indexExists(IndexDefinition index)
	{
		if (index == null) {
			logger.severe("Cannot check the existence of a null Index");
			return false;
		}
		try {
			return client.admin().indices().prepareExists(index.getSimpleValue()).get().isExists();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to check if index exists", e);
			return false;
		}
	}

	/**
	 * 
	 * @param index
	 *            SearchIndexDefinition
	 * @return boolean if the index exists or not
	 */
	public boolean indexExists(SearchIndexDefinition index)
	{
		if (index == null) {
			logger.severe("Cannot check the existence of a null Index");
			return false;
		}
		try {
			return client.admin().indices().prepareExists(index.getSimpleIndex().getSimpleValue()).get().isExists();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to check if index exists", e);
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
	public boolean indexProperlyConfigured(SearchIndexDefinition index)
	{

		if (index == null) {
			return false;
		}

		if (indexExists(index)) {

			// compare the expected index fields to the actual index fields
			Map<String, String> expected = new HashMap<String, String>();
			index.getSimpleFields().forEach(fields -> {
				expected.put(fields.getSimpleFieldName().getSimpleName(), fields.getSimpleType().getSimpleCode());
			});

			try {
				GetMappingsResponse response = client.admin().indices().prepareGetMappings(index.getSimpleIndex().getSimpleValue()).get();

				String json = response.getMappings().get(index.getSimpleIndex().getSimpleValue()).get(ELASTICSEARCH_DEFAULT_TYPE).source().string();

				Map<String, String> actual = new HashMap<String, String>();

				new ObjectMapper().readTree(json).get(ELASTICSEARCH_DEFAULT_TYPE).get("properties").fields().forEachRemaining(fieldMapping -> {
					actual.put(fieldMapping.getKey(), fieldMapping.getValue().get("type").asText());
				});
				return expected.equals(actual);

			} catch (Exception e) {
				logger.log(Level.SEVERE, String.format("Failed to get the index mapping for index %s", index.getSimpleIndex().getSimpleValue()), e);
			}
		}

		return false;

	}

	private boolean createIndex(SearchIndexDefinition index)
	{
		if (index == null) {
			logger.severe("Cannot create a null Index");
			return false;
		}

		try {
			XContentBuilder mappingBuilder = jsonBuilder().startObject().startObject(ELASTICSEARCH_DEFAULT_TYPE).startObject("properties");

			for (SearchIndexFieldDefinition field : index.getSimpleFields()) {
				mappingBuilder.startObject(field.getSimpleFieldName().getSimpleName()).field("type", field.getSimpleType().getSimpleCode()).endObject();
			}

			mappingBuilder.endObject().endObject().endObject();

			CreateIndexResponse createResponse = client.admin().indices().prepareCreate(index.getSimpleIndex().getSimpleValue()).addMapping(ELASTICSEARCH_DEFAULT_TYPE, mappingBuilder).get();

			if (!createResponse.isAcknowledged()) {
				logger.severe(String.format("Index Creation not acknowledged for index %s", index.getSimpleIndex().getSimpleValue()));
				return false;
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, String.format("Failed to generate mapping json for index %s", index.getSimpleIndex().getSimpleValue()), e);
			return false;
		}

		return true;
	}

	private boolean deleteIndex(SearchIndexDefinition index)
	{
		if (index == null) {
			logger.severe("Cannot delete a null Index");
			return false;
		}

		try {
			DeleteIndexResponse deleteResponse = client.admin().indices().prepareDelete(index.getSimpleIndex().getSimpleValue()).get();
			if (!deleteResponse.isAcknowledged()) {
				logger.severe(String.format("Index Deletion not acknowledged for index %s", index.getSimpleIndex().getSimpleValue()));
				return false;
			}
		} catch (Exception e) {
			logger.severe(String.format("Index Deletion failed for index %s", index.getSimpleIndex().getSimpleValue()));
			return false;
		}
		return true;

	}

	// on shutdown
	public void closeClient()
	{
		client.close();
	}

	/**
	 * Upsert if the index doesnt exist or is not properly configured already
	 * 
	 * @param index
	 *            SearchIndexDefinition
	 * @return boolean if the upsert was successful or not
	 */
	public boolean upsertIndex(SearchIndexDefinition index)
	{

		if (index == null) {
			logger.severe("Cannot upsert a null Index");
			return false;
		}

		// if it exists and is not configured correctly delete and add
		if (indexExists(index)) {
			if (!indexProperlyConfigured(index)) {
				if (deleteIndex(index)) {
					return createIndex(index);
				} else {
					// deletion failed
					return false;
				}
			}
		} else {
			// index is new
			return createIndex(index);
		}

		// index exists and already configured correctly
		logger.info("No upsert needed");
		return true;
	}

}