		try {
			indexDirectory = FSDirectory.open(new File(ciFactory.getProperty(INDEXPATH)));
			indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
			booleanQuery = generateSourceCodeQuery(condition);
			TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
			indexSearcher.search(booleanQuery, collector);
			totalHits = collector.getTotalHits();
		} catch (Exception e) {
			log.debug("open indexDirectory failure: {}", e);
		}
