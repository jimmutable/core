package org.jimmutable.core.serialization;

import org.jimmutable.core.serialization.Format;
import org.jimmutable.core.serialization.reader.ObjectParseTree;
import org.jimmutable.core.serialization.writer.ObjectWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PrimativeReadWriteTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PrimativeReadWriteTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PrimativeReadWriteTest.class );
    }

    public void testByte()
    {
    	testObject(Byte.MAX_VALUE);
    	testObject(Byte.MIN_VALUE);
    	testObject(new Byte((byte)1));
    	testObject(new Byte((byte)0));
    }
    
    public void testShort()
    {
    	testObject(Short.MAX_VALUE);
    	testObject(Short.MIN_VALUE);
    	testObject(new Short((short)1));
    	testObject(new Short((short)0));
    }
    
    public void testInt()
    {
    	testObject(Integer.MAX_VALUE);
    	testObject(Integer.MIN_VALUE);
    	testObject(new Integer(1));
    	testObject(new Integer(0));
    }
    
    public void testLong()
    {
    	testObject(Long.MAX_VALUE);
    	testObject(Long.MIN_VALUE);
    	testObject(new Long(1));
    	testObject(new Long(0));
    	testObject(new Long(System.currentTimeMillis()));
    }
    
    public void testFloat()
    {
    	testObject(Float.MAX_VALUE);
    	testObject(Float.MIN_VALUE);
    	testObject(Float.NaN);
    	testObject(Float.NEGATIVE_INFINITY);
    	testObject(Float.POSITIVE_INFINITY);
    	
    	testObject(new Float(1));
    	testObject(new Float(0));
    	testObject(new Float(2.8/2.1));
    }
    
    public void testDouble()
    {
    	testObject(Double.MAX_VALUE);
    	testObject(Double.MIN_VALUE);
    	testObject(Double.NaN);
    	testObject(Double.NEGATIVE_INFINITY);
    	testObject(Double.POSITIVE_INFINITY);
    	
    	testObject(new Double(1));
    	testObject(new Double(0));
    	testObject(new Double(2.8/2.1));
    }
    
    public void testStings()
    {
    	testObject("Hello World");
    	testObject("");
    	
    	testObject("Fisher & Paykel");
    	
    	testObject("{ foo : \"bar\" }");
    	
    	testObject("<html></html>");
    	
    	testObject("Hello There \u00a9");
    	
    	// The acid string...
    	testObject(createNonBase64AcidString());
    	testObject(createAcidString());
    }
    
    static public String createAcidString()
    {
    	StringBuilder ret = new StringBuilder();
    	
    	ret.append("\n");
    	
    	for ( int i = 0; i < 10_000; i++ )
    	{
    		ret.append((char)i);
    	}
    	
    	return ret.toString();
    }
    
    static public String createNonBase64AcidString()
    {
    	StringBuilder ret = new StringBuilder();
    	
    	ret.append("\n");
    	
    	for ( int i = 0; i < 10_000; i++ )
    	{
    		char ch = (char)i;
    		if ( i > 32 ) { ret.append(ch); }
    		if ( ch == '\t' ) { ret.append(ch); }
    		if ( ch == '\n' ) { ret.append(ch); }
    		if ( ch == '\r' ) { ret.append(ch); }
    	}
    	
    	return ret.toString();
    }
    
    private void testObject(Object obj)
    {
    	testObject(Format.XML,obj);
    	testObject(Format.XML_PRETTY_PRINT,obj);
    	testObject(Format.JSON,obj);
    	testObject(Format.JSON_PRETTY_PRINT,obj);;
    }
    
    private void testObject(Format format, Object obj)
    {
    	/*if ( obj instanceof Long )
    	{
    		System.out.println(String.format("%s: %d", format, obj));
    	}*/
    	String serialized_data = ObjectWriter.serialize(format, obj);
    	assert(serialized_data != null);
    	
    	if ( obj.equals("") )
    	{
    		System.out.println(serialized_data);
    	}
    	
    	Object from_reader = ObjectParseTree.deserialize(serialized_data);
    	
    	assertEquals(obj,from_reader);
    }
}