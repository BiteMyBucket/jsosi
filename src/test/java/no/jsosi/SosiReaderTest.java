package no.jsosi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SosiReaderTest extends TestCase {

    public void testAddress() throws IOException {
        File file = new File("src/test/resources/0219Adresser.SOS");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25833", r.getCrs());
        assertEquals(0.01, r.getXYFactor());

        List<Feature> features = new ArrayList<Feature>();
        Feature feature;
        while ((feature = r.nextFeature()) != null) {
            features.add(feature);
        }

        assertEquals(33545, features.size());

        Feature f = features.get(0);
        assertEquals("Hans Hanssens vei !nocomment", f.get("GATENAVN"));
        assertEquals("SNARØYA", f.get("POSTNAVN"));
        assertEquals("0219", f.get("KOMM"));
        assertEquals("4", f.get("HUSNR"));
        assertNull(f.get("NØ"));
        assertNotNull(f.getGeometry());
        assertTrue(f.getGeometry() instanceof Point);
        assertEquals(1, f.getCoordinateCount());
        assertEquals(253673.99, f.getGeometry().getCoordinates()[0].x, 0.01);
        assertEquals(6645919.76, f.getGeometry().getCoordinates()[0].y, 0.01);

        r.close();

    }

    public void testVbase() throws IOException {
        File file = new File("src/test/resources/Vbase_02.SOS");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25833", r.getCrs());

        Feature f1 = r.nextFeature();
        assertEquals("P V 99834", f1.get("VNR"));
        assertNull(f1.get("GATENAVN"));
        assertEquals(12, f1.getCoordinateCount());
        assertTrue(f1.getGeometry() instanceof LineString);

        Feature f2 = r.nextFeature();
        assertEquals("Åsveien", f2.get("GATENAVN"));
        assertEquals(15, f2.getCoordinateCount());
        assertTrue(f2.getGeometry() instanceof LineString);

        int count = 1;
        Feature f = null;
        while ((f = r.nextFeature()) != null) {
            count++;
            assertNotNull(f.getGeometry());
        }

        assertTrue(count > 10000);

        r.close();

    }

    public void testArealdekke() throws IOException {
        File file = new File("src/test/resources/1421_Arealdekke.sos");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25832", r.getCrs());

        Feature f1 = r.nextFeature();
        assertEquals("10000101", f1.get("OPPDATERINGSDATO"));
        assertEquals("ÅpentOmråde", f1.get("OBJTYPE"));
        assertNull(f1.get("GATENAVN"));
        assertTrue(f1.getGeometry() instanceof Polygon);
        assertTrue(f1.getGeometry().isValid());

        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        
        Feature f = null;
        Feature f5763 = null;
        while ((f = r.nextFeature()) != null) {
            String objtype = (String) f.get("OBJTYPE");
            assertNotNull(objtype);
            
            count++;
            objtypes.add(objtype);

            if (f.getId().intValue() == 5763) {
                f5763 = f;
            }

            if (f.getGeometryType() != GeometryType.KURVE) {
                continue;
            }
        }

        assertNotNull(f5763);
        assertEquals(5763, f5763.getId().intValue());
        assertEquals("Innsjø", f5763.get("OBJTYPE"));
        assertTrue(f5763.getGeometry() instanceof Polygon);
        assertTrue(f5763.getGeometry().isValid());
        
        assertEquals(21313, count);
        assertEquals(27, objtypes.size());

        r.close();
    }

    public void testNavnISO() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25832", ri.getCrs());

        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertEquals("Skrivemåte", fi.get("OBJTYPE"));
            assertNotNull(fi.getGeometry());
            assertTrue(fi.getGeometry().isValid());
        }
        ri.close();
    }

    public void testInputStream() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(new FileInputStream(file));
        assertEquals("EPSG:25832", ri.getCrs());
        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
        }
        ri.close();
    }

    public void testProgress() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25832", ri.getCrs());
        assertEquals(0f, ri.getProgress(), 0.0001f);

        for (int i = 0; i < 1500; i++) {
            assertNotNull(ri.nextFeature());
        }
        assertEquals(0.571257f, ri.getProgress(), 0.0001f);

        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
        }
        assertEquals(1f, ri.getProgress(), 0.0001f);
        ri.close();

    }

    public void testBOM() throws IOException {
        File file = new File("src/test/resources/BOM_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
        }
        assertEquals(5557, count);
        ri.close();
    }
    
    public void testISOUTF8() throws IOException {
        File file = new File("src/test/resources/ISO_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
        }
        assertEquals(2916, count);
        ri.close();
    }
    
    public void testEmptyLine() throws IOException {
        File file = new File("src/test/resources/0633_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            
            for (Map.Entry<String, Object> e : fi.getAttributeMap().entrySet()) {
                String key = e.getKey();
                if ("SSR".equals(key)) {
                    continue;
                }
                assertNotNull("feature should not have null key. " + fi.getAttributeMap().toString(), key);
                assertTrue("feature should not have empty key. " + fi.getAttributeMap().toString(), key.length() > 0);
                assertNotNull("key '" + key + "' should not have null value", e.getValue());
            }
            
            
            count++;
        }
        assertEquals(3844, count);
        ri.close();
    }
    
    public void testMissingGeometry() throws IOException {
        File file = new File("src/test/resources/0540_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            if ("Fønhuskoia".equals(fi.get("STRENG"))) {
                assertTrue(fi.getGeometry().isEmpty());
            } else {
                assertFalse(fi.getGeometry().isEmpty());
            }
            count++;
        }
        assertEquals(2304, count);
        ri.close();
    }

    public void testEnheterGrunnkrets() throws Exception {
        File file = new File("src/test/resources/STAT_enheter_grunnkretser.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(8, objtypes.size());
        assertEquals(79724, count);
        ri.close();
    }

}
