package simple.xml.load;

import junit.framework.TestCase;
import simple.xml.stream.Node;
import simple.xml.stream.NodeMap;
import simple.xml.Serializer;
import simple.xml.Attribute;
import simple.xml.Element;
import simple.xml.Root;
import java.util.Map;

public class StrategyTest extends TestCase {

   private static final String ELEMENT_NAME = "example-attribute";        

   private static final String ELEMENT =
   "<?xml version=\"1.0\"?>\n"+
   "<root key='attribute-example-key' example-attribute='simple.xml.load.StrategyTest$ExampleExample'>\n"+
   "   <text>attribute-example-text</text>  \n\r"+
   "</root>";

   @Root(name="root")
   private static abstract class Example {

      public abstract String getValue();   
      
      public abstract String getKey();
   }
   
   private static class ExampleExample extends Example {

      @Attribute(name="key")           
      public String key;           
           
      @Element(name="text")
      public String text;           

      public String getValue() {
         return text;              
      }
      
      public String getKey() {
         return key;
      }
   }

   public class ExampleStrategy implements Strategy {

      public int writeRootCount = 0;           

      public int readRootCount = 0;

      private StrategyTest test;

      public ExampleStrategy(StrategyTest test){
         this.test = test;              
      }

      public Class readRoot(Class type, NodeMap root, Map map) throws Exception {
         readRootCount++;
         return readElement(type, root, map);              
      }

      public Class readElement(Class type, NodeMap node, Map map) throws Exception {
         Node value = node.remove(ELEMENT_NAME);

         if(readRootCount != 1) {
            test.assertTrue("Root must only be read once", false);                 
         }         
         if(value != null) {
            return Class.forName(value.getValue());
         }
         return null;
      }         

      public void writeRoot(Class field, Object value, NodeMap root, Map map) throws Exception {                       
         writeRootCount++;              
         writeElement(field, value, root, map);              
      }              

      public void writeElement(Class field, Object value, NodeMap node, Map map) throws Exception {
         if(writeRootCount != 1) {
            test.assertTrue("Root must be written only once", false);                 
         }                 
         if(field != value.getClass()) {                       
            node.put(ELEMENT_NAME, value.getClass().getName());
         }            
      }
   }

   public void testExampleStrategy() throws Exception {    
      ExampleStrategy strategy = new ExampleStrategy(this);           
      Serializer persister = new Persister(strategy);
      Example example = persister.read(Example.class, ELEMENT);
      
      assertTrue(example instanceof ExampleExample);
      assertEquals(example.getValue(), "attribute-example-text");
      assertEquals(example.getKey(), "attribute-example-key");
      assertEquals(1, strategy.readRootCount);
      assertEquals(0, strategy.writeRootCount);
      
      persister.write(example, System.err);
      
      assertEquals(1, strategy.readRootCount);
      assertEquals(1, strategy.writeRootCount);
   }
}
