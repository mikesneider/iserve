package es.usc.citius.composit.importer.wsc.wscxml;


import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.net.URI;
import java.util.*;

public class WSCImporterTest {
    private static final Logger log = LoggerFactory.getLogger(WSCImporterTest.class);

    @Test
    public void testImport() throws Exception {

        // Add all the test collections
        BasicConfigurator.configure();
        log.info("Transforming test collections");
            File services = new File(getClass().getClassLoader().getResource("services.xml").getFile());

            log.info("Transforming service {}", services.getAbsolutePath());
                try {
                    List<Service> result = Transformer.getInstance().transform(services, null, WSCImporter.mediaType);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(158, result.size());
                } catch (Exception e) {
                    log.error("Problems transforming the service. Continuing", e);
                }
            }

    }

