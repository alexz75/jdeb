/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vafer.jdeb;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.vafer.jdeb.ar.ArEntry;
import org.vafer.jdeb.ar.ArInputStream;
import org.vafer.jdeb.descriptors.PackageDescriptor;
import org.vafer.jdeb.producers.DataProducerArchive;
import org.vafer.jdeb.producers.DataProducerDirectory;

public final class DataProducerTestCase extends TestCase {

	public void testCreation() throws Exception {

		final Processor processor = new Processor(new Console() {
			public void println(String s) {
			}
		}, null);
		
		final File control = new File(getClass().getResource("deb/control/control").getFile());
		final File archive = new File(getClass().getResource("deb/data.tgz").getFile());
		final File directory = new File(getClass().getResource("deb/data").getFile());
		
		final DataProducer[] data = new DataProducer[] {
				new DataProducerArchive(archive, null, null, null),
				new DataProducerDirectory(directory, null, null, null)
		};
		
		final File deb = File.createTempFile("jdeb", ".deb");
		
		final PackageDescriptor packageDescriptor = processor.createDeb(new File[] { control }, data, deb);
		
		assertTrue(packageDescriptor.isValid());
		
		final Set filesInDeb = new HashSet();
		
		final ArInputStream ar = new ArInputStream(new FileInputStream(deb));
		while(true) {
			final ArEntry arEntry = ar.getNextEntry();
			if (arEntry == null) {
				break;
			}
			
			if ("data.tar.gz".equals(arEntry.getName())) {
				
				final TarInputStream tar = new TarInputStream(new GZIPInputStream(ar));
				
				while(true) {
					final TarEntry tarEntry = tar.getNextEntry();
					if (tarEntry == null) {
						break;
					}
					
					filesInDeb.add(tarEntry.getName());
				}
				
				break;
			}
			for (int i = 0; i < arEntry.getLength(); i++) {
				ar.read();
			}
		}
		
		assertTrue("" + filesInDeb, filesInDeb.contains("/test/testfile"));
		assertTrue("" + filesInDeb, filesInDeb.contains("/test/testfile2"));
		
		deb.delete();
	}
}
