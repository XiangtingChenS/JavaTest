package com.gss.adm.scd.discovery.runner;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Iterables;
import com.gss.adm.api.enums.ScheduleTypeEnum;
import com.gss.adm.core.wsclient.domian.ProjectDomain;
import com.gss.adm.core.wsclient.domian.ProjectModelDefinitionDomain;
import com.gss.adm.core.wsclient.domian.ProjectModelDomain;
import com.gss.adm.core.wsclient.domian.simple.AttributeDefinitionSimpleDomain;
import com.gss.adm.core.wsclient.domian.simple.RelationRuleSimpleDomain;
import com.gss.adm.scd.discovery.DiscoveryExecuter;
import com.gss.adm.scd.schedule.AdmScheduleInfoBuilder;
import com.gss.adm.scd.service.AttributeDefinitionFileService;
import com.gss.adm.scd.service.ProjectModelDefinitionFileService;
import com.gss.adm.scd.service.RelationRuleFileService;
import com.gss.adm.scd.webservice.AttributeDefinitionWebService;
import com.gss.adm.scd.webservice.LogScheduleWebService;
import com.gss.adm.scd.webservice.ProjectModelDefinitionWebService;
import com.gss.adm.scd.webservice.ProjectModelWebService;
import com.gss.adm.scd.webservice.ProjectWebService;
import com.gss.adm.scd.webservice.RelationRuleWebService;

import lombok.Getter;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:adm-scd.xml")
public class DiscoveryExecuterIntegrationTest {

	@Getter
	private Long projectId = 34792L;

	@Autowired
	private ProjectWebService projectService;

	@Autowired
	private ProjectModelDefinitionWebService projectModelDefinitionWebService;
	
	@Autowired
	private ProjectModelDefinitionFileService projectModelDefinitionFileService;
	
	@Autowired
	private AttributeDefinitionWebService attributeDefinitionWebService;
	
	@Autowired
	private AttributeDefinitionFileService attributeDefinitionFileService;
	
	@Autowired
	private RelationRuleWebService relationRuleWebService;
	
	@Autowired
	private RelationRuleFileService relationRuleFileService;
	
	@Autowired
	ProjectModelWebService projectModelWebService;
	
	@Autowired
	private LogScheduleWebService logScheduleService;
	
	@Autowired
	private DiscoveryExecuter discoveryExecuter;

	@Test
	public void testExecute() {
		ProjectDomain project = projectService.getProject(getProjectId());
		String projectKey = project.getCode();
		scheduleLogPending();
		discoveryExecuter.execute(projectKey);
	}
	
	@Test
	public void test() {
		List<ProjectModelDefinitionDomain> strategies = projectModelDefinitionWebService.findProjectModelDefinitionsByModelTypeId(2);
		projectModelDefinitionFileService.saveProjectModelDefinitions(strategies);

		List<ProjectModelDefinitionDomain> advancedSettings = projectModelDefinitionWebService.findProjectModelDefinitionsByModelTypeId(5);
		projectModelDefinitionFileService.saveProjectModelDefinitions(advancedSettings);
	
		List<AttributeDefinitionSimpleDomain> attributeDefinitions = attributeDefinitionWebService.findAll();
		attributeDefinitionFileService.saveAttributeDefinitions(attributeDefinitions);
		
		List<RelationRuleSimpleDomain> relationRules = relationRuleWebService.findAllRelationRules();
		relationRuleFileService.saveRelationRules(relationRules);
		
	}
	
	private void scheduleLogPending(){
		List<ProjectModelDomain> schedules = projectModelWebService
				.findProjectModelsByProjectIdAndModelDefinitionId(projectId, 1L);
		ProjectModelDomain schedule = Iterables.getFirst(schedules, null);

		if (schedule == null) {
			System.err.println("Schedule is null");
		}
		String scheduleId = AdmScheduleInfoBuilder.getScheduleId(schedule);
		String scheduleName = AdmScheduleInfoBuilder.getScheduleName(schedule);
		logScheduleService.logPending(projectId, scheduleId, scheduleName, ScheduleTypeEnum.DISCOVERY);
	}
}
