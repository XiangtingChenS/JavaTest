	BooleanQuery keywordsQuery = new BooleanQuery();
		List<String> keywords = Arrays.asList(keyWordAry);

		Query entityNameQuery = createKeywordsQuery(ENTITY_NAME, keywords, MUST);
		keywordsQuery.add(entityNameQuery, SHOULD);

		Query entityCodeQuery = createKeywordsQuery(ENTITY_CODE, keywords, MUST);
		keywordsQuery.add(entityCodeQuery, SHOULD);
