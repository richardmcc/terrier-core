/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is BatchIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2016 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.utility.ApplicationSetup;
/** Abstract class for all code that set up the batch indexers */
public abstract class BatchIndexing {


	public static class Command extends CLIParsedCLITool
	{
		@Override
		protected Options getOptions()
		{
			Options options = new Options();
			options.addOption(Option.builder("j")
					.argName("singlepass")
					.longOpt("singlepass")
					.desc("use the single-pass indexer")
					.build());
			options.addOption(Option.builder("p")
					.argName("threads")
					.longOpt("parallel")
					.optionalArg(true)
					.desc("use multiple threads for the indexer, and optionally specify the number of threads")
					.build());
			options.addOption(Option.builder("b")
					.argName("blocks")
					.longOpt("blocks")
					.desc("record block (positions) in the index")
					.build());
			return options;
		}
		
		
		@Override
		public String commandname() {
			return "batchindexing";
		}

		@Override
		public String helpsummary() {
			return "allows a static collection of documents to be indexed";
		}

		@Override
		public int run(String[] args) {
			
			CommandLineParser parser = new DefaultParser();
			try {
				CommandLine line = parser.parse(getOptions(), args);
				
				BatchIndexing indexing;
				if (line.hasOption("parallel"))
				{
					indexing = new ThreadedBatchIndexing(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX, line.hasOption("singlepass"));
					String threads = line.getOptionValue("parallel");
					if (threads != null)
						((ThreadedBatchIndexing)indexing).setMaxThreads(Integer.parseInt(threads));
				}
				else
				{
					indexing = line.hasOption("singlepass")
							? new TRECIndexingSinglePass(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)
							: new TRECIndexing(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
				}
				indexing.blocks = line.hasOption("blocks");
				indexing.index();
			} catch (ParseException e) {
				 System.err.println( "Usage problem:" + e);
				 System.err.println( help() );
			}
			
			return 0;
		}
		
	}

	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(BatchIndexing.class);
	protected final String path;
	protected final String prefix;
	protected boolean blocks = ApplicationSetup.BLOCK_INDEXING;

	public BatchIndexing(String _path, String _prefix) {
		super();
		this.path = _path;
		this.prefix = _prefix;
	}

	public abstract void index();
	
}
