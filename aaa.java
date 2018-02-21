package com.gss.adm.web.jsf.actions;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.gss.adm.api.lang.FileUtils2;
import com.gss.adm.api.spring.security.AuthenticationUtils;
import com.gss.adm.api.utils.LogUtils;
import com.gss.adm.core.condition.FullTextEntitySearchCondition;
import com.gss.adm.core.condition.PaginationCondition;
import com.gss.adm.core.model.Entitydefinition;
import com.gss.adm.core.model.Enttity;
import com.gss.adm.core.model.FullTextSearchEntity;
import com.gss.adm.core.service.EntityDefinitionService;
import com.gss.adm.core.service.EntityService;
import com.gss.adm.core.service.FullTextSearchService;
import com.gss.adm.core.utils.CollectionUtils;
import com.gss.adm.web.view.AttributeDefinitionView;
import com.gss.adm.web.view.EntityDefinitionView;
import com.gss.adm.web.view.EntityFullTextSearchView;
import com.gss.adm.web.view.FullTextSearchDefinitionView;
import com.gss.adm.web.view.FullTextSearchOtherInfoView;
import com.gss.adm.web.view.FullTextSearchView;
import com.gss.adm.web.view.convert.AttributeDefinitionViewConverter;
import com.gss.adm.web.view.convert.EntityDefinitionViewConverter;
import com.gss.adm.web.view.convert.EntityFullTextViewConverter;
import com.gss.adm.web.view.convert.FullTextSearchDefinitionConverter;
import com.gss.adm.web.view.convert.FullTextSearchEntityConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Taoyu_Wu
 */
@Slf4j
@Component
@Path("fulltext")
public class FullTextSearchAction {

	@Autowired
	private FullTextSearchService fullTextSearchService;

	@Autowired
	private EntityDefinitionService entityDefinitionService;
	
	@Autowired
	private EntityService entityService;

	@Autowired
	private FullTextSearchEntityConverter fullTextSearchEntityConverter;

	@Autowired
	private FullTextSearchDefinitionConverter fullTextSearchDefinitionConverter;

	@Autowired
	private EntityFullTextViewConverter entityFullTextViewConverter;

	@Autowired
	private AttributeDefinitionViewConverter attributeDefinitionViewConverter;
	
	@Autowired
	private EntityDefinitionViewConverter entityDefinitionViewConverter;

	@GET
	@Path("/entityDefinitionInfo")
	@Consumes(MediaType.APPLICATION_JSON)
	public FullTextSearchDefinitionView prepareDefinitionInfo(@QueryParam("keyword") String keyword,
			@QueryParam("targetProjectId") long targetProjectId) {

		log.debug("Search keyword: {}", keyword);
		
		FullTextSearchDefinitionView fullTextSearchDefinitionView = new FullTextSearchDefinitionView();

		String username = AuthenticationUtils.getUsernameFromSecurityContext();

		FullTextEntitySearchCondition condition = new FullTextEntitySearchCondition();
		condition.setUsername(username);
		condition.setKeyword(keyword);
		condition.setTargetProjectId(targetProjectId);
		
		Stopwatch logStart = LogUtils.logStart("getEntityDefinitionCountMap  start");
		Map<Integer, Integer> entityDefinitionCountMap = fullTextSearchService.getEntityDefinitionCountMap(condition);
		LogUtils.logStop(logStart,"getEntityDefinitionCountMap stop");
		
		fullTextSearchDefinitionView.setEntityDefinitionCountMap(entityDefinitionCountMap);
		
		// set all entity definition name's map
		List<Entitydefinition> allEntityDefinition = entityDefinitionService.findAllEntityDefinitions();
		Map<Integer, EntityDefinitionView> entityDefinitionMap = Maps.newHashMap();
		for (Entitydefinition entitydefinition : allEntityDefinition) {
			EntityDefinitionView view = entityDefinitionViewConverter.convert(entitydefinition);
			entityDefinitionMap.put(entitydefinition.getEntityDefinitionID(), view);
		}
		fullTextSearchDefinitionView.setEntityDefinitionMap(entityDefinitionMap);
	
		return fullTextSearchDefinitionView;
	}

	@GET
	@Path("keyword/{keyword}")
	@Consumes(MediaType.APPLICATION_JSON)
	public FullTextSearchView searchKeyWord(@PathParam("keyword") String keyword) {

		log.debug("Search keyword: {}", keyword);

		String username = AuthenticationUtils.getUsernameFromSecurityContext();

		FullTextEntitySearchCondition condition = new FullTextEntitySearchCondition();
		condition.setUsername(username);
		condition.setKeyword(keyword);

		FullTextSearchEntity searchEntity = fullTextSearchService.searchEntityByKeyword(condition);

		return fullTextSearchEntityConverter.convert(searchEntity);
	}

	@GET
	@Path("/keyword/{keyword}/definition/{entityDefinitionId}/targetProjectId/{targetProjectId}/paging")
	@Consumes(MediaType.APPLICATION_JSON)
	public FullTextSearchView searchKeyWordByPaging(@PathParam("keyword") String keyword,
			@PathParam("entityDefinitionId") Integer entityDefinitionId,
			@PathParam("targetProjectId") Long targetProjectId, @QueryParam("page") int page,
			@QueryParam("pageSize") int pageSize, @QueryParam("skip") int skip, @QueryParam("take") int take) {

		log.debug("keyword: {}, entityDefinitionId: {}", keyword, entityDefinitionId);
		log.debug("target ProjectId: {}", targetProjectId);
		log.debug("page: {}, pageSize: {}, skip: {}, take: {}", page, pageSize, skip, take);

		String username = AuthenticationUtils.getUsernameFromSecurityContext();

		FullTextEntitySearchCondition condition = new FullTextEntitySearchCondition();
		condition.setUsername(username);
		condition.setKeyword(keyword);
		condition.setEntityDefinitionId(entityDefinitionId);
		condition.setTargetProjectId(targetProjectId);

		PaginationCondition pagination = new PaginationCondition();
		pagination.setFirstResult(page);
		pagination.setMaxResults(pageSize);

		condition.setPagination(pagination);

		FullTextSearchView resultView = new FullTextSearchView();

		Stopwatch logStart = LogUtils.logStart("searchEntityFromSourceCodeByKeyword start");
		// SourceCode Part:
		FullTextSearchEntity sourceCodeSearchEntity = fullTextSearchService
				.searchEntityFromSourceCodeByKeyword(condition, 0);
		LogUtils.logStop(logStart, "searchEntityFromSourceCodeByKeyword stop");
		
		 logStart = LogUtils.logStart("aaa start");
		if (sourceCodeSearchEntity != null) {
			List<Enttity> sourceCodeEntity = sourceCodeSearchEntity.getResultEntities();

			if (CollectionUtils.isNotEmpty(sourceCodeEntity)) {
				List<EntityFullTextSearchView> entityViews = entityFullTextViewConverter.convert(sourceCodeEntity,
						condition.getKeyword());
				resultView.setModels(entityViews);
				resultView.setTotal(sourceCodeSearchEntity.getTotal());
				LogUtils.logStop(logStart, "inside aaa  stop");
				return resultView;
			}
		}
		LogUtils.logStop(logStart, "outside aaa stop");
		return resultView;
	}

	@GET
	@Path("/entityId/{entityId}/keyword/{keyword}/")
	@Consumes(MediaType.APPLICATION_JSON)
	public FullTextSearchOtherInfoView searchEntityOtherInfoByEntityIdAndKeyword(@PathParam("entityId") Integer entityId,
			@PathParam("keyword") String keyword) {
		
		FullTextSearchOtherInfoView fullTextSearchOtherInfoView = new FullTextSearchOtherInfoView();
		
		Enttity entity = entityService.getEntityByEntityId(entityId);
		Entitydefinition entityDefinition = entity.getEntityDefinition();
		List<AttributeDefinitionView> attributeDefinitionViews = attributeDefinitionViewConverter
				.convert(entityDefinition.getAttributeDefinitions());
		for (int i=0; i<attributeDefinitionViews.size(); i++){
			AttributeDefinitionView attributeDefinitionView = attributeDefinitionViews.get(i);
			if (! attributeDefinitionView.isSearchable()){
				attributeDefinitionViews.remove(i);
				i--;
			}
		}
		fullTextSearchOtherInfoView.setAttributeDefinitionViews(attributeDefinitionViews);
		fullTextSearchOtherInfoView.setFileContent(getContentByKeyword(entity, keyword));

		return fullTextSearchOtherInfoView;
	}
	
	private String getContentByKeyword(Enttity entity, String keyWords) {
		String location = entity.getAttributeValue("location");
		if (StringUtils.isBlank(location)){
			return StringUtils.EMPTY;
		}
		String[] keyWordAry = keyWords.split("(\\s|\\+)");
		
		File file = Paths.get(location).toFile();
		String matchStr = StringUtils.EMPTY;
        if (file.exists()) {
            Scanner contentScanner = null;
            try {
            	String encoding = FileUtils2.getEncoding(file);
            	contentScanner = new Scanner(file, encoding);
            	
                // 取得pattern為keyword的 的那行程式碼
            	for (String keyWord: keyWordAry){
            		Pattern regexPattern = Pattern.compile("(?i)" + keyWord);
            		while (contentScanner.hasNextLine()) {
            			String lineContent = contentScanner.nextLine();
            			Matcher regexMatcher = regexPattern.matcher(lineContent);
            			if (regexMatcher.find()) {
            				// 記錄所取字串的內容與行號
            				matchStr = lineContent;
            				break;
            			}
            		}
            		if (StringUtils.isNotBlank(matchStr)){
            			break;
            		}
            	}
            } catch (Exception e) {
                log.debug("Scan file failed. {}", e);
                return StringUtils.EMPTY;
            } finally {
            	if(contentScanner != null) {
            		contentScanner.close();
            	}
            }
        }
		return matchStr;
	}
}
