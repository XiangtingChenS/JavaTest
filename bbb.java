	@Override
	public List<Enttity> countEntityByKeyword(Long projectId, IndexSearcher indexSearcher, ScoreDoc[] scoreDocs) {

		List<Enttity> entities = Lists.newArrayList();
		Set<String> identifiers = Sets.newHashSet();
		try {
			for (int i = 0; i < scoreDocs.length; i++) {
				Document hitDoc = indexSearcher.doc(scoreDocs[i].doc);
				identifiers.add(hitDoc.get(ENTITY_IDENTIFIER));
			}
		} catch (Exception e) {
			log.debug("indexSearcher get identifier column failure: {}", e);
		}

		if (CollectionUtils2.isNotEmpty(identifiers)) {
			if (projectId != 0) {
				entities = entityService.findByProjectIdAndIdentifiers(projectId, identifiers);
			} else {
				entities = entityService.findByIdentifiers(identifiers);
			}
		}
		return entities;
	}
