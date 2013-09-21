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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;


import es.usc.citius.composit.importer.wsc.wscxml.model.taxonomy.XMLConcept;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 * @author pablo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLOutputs {
    @XmlElement(name = "concept")
    private ArrayList<XMLConcept> outputs;

    public ArrayList<XMLConcept> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<XMLConcept> outputs) {
        this.outputs = outputs;
    }

}
