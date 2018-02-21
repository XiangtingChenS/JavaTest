package com.gss.adm.core.service;


import java.util.List;
import java.util.Map;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import com.gss.adm.core.condition.FullTextEntitySearchCondition;
import com.gss.adm.core.form.FullTextSearchEntityForm;
import com.gss.adm.core.model.Enttity;
import com.gss.adm.core.model.FullTextSearchEntity;

public interface FullTextSearchService {

    void reindex();
    
    void sourceCodeReindex(long projectId);
    
    FullTextSearchEntity searchEntityByKeyword(FullTextEntitySearchCondition fullTextSearchForm);
    
    Map<Integer, Integer> getEntityDefinitionCountMap(FullTextEntitySearchCondition fullTextSearchForm);
    
    List<Enttity> countEntityByKeyword(Long projectId, IndexSearcher indexSearcher, ScoreDoc[] scoreDocs);
    
	FullTextSearchEntity searchEntityFromSourceCodeByKeyword(FullTextEntitySearchCondition condition, int dataBaseResultCount);

	List<Enttity> searchEntityFromSourceCodeByKeywordAndProjectId(String methodName, Long projectId, int entitySize);
	
	List<Enttity> searchEntitiesByProjectIdAndDefinitionIdsAndKeywords(FullTextSearchEntityForm form);
	
	List<Enttity> searchEntitySourceCodeByKeywordAndProjectId(Long projectId, List<String> detailedTerms, int entitySize, int searchMode);
}
