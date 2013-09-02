/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.usc.citius.composit.importer.wsc.wscxml;


import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.util.List;

//import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

// TODO; Move test to sal-core or another project to avoid cyclic dependencies with the module.
// (if a test is added to sal-core using the WSC importer, the WSC importer cannot use sal-core!)
public class WSCImporterTest {
    private static final Logger log = LoggerFactory.getLogger(WSCImporterTest.class);

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        //ManagerSingleton.getInstance().clearRegistry();
    }

    @Test
    public void testTransform() throws Exception {
        // Add all the test collections
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

