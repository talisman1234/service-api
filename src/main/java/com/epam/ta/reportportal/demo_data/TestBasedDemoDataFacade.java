/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static java.util.stream.Collectors.toList;

@Service
public class TestBasedDemoDataFacade extends DemoDataCommonService implements DemoDataFacade {

    private static final StatisticsCalculationStrategy strategy = StatisticsCalculationStrategy.TEST_BASED;

    @Value("classpath:demo/demo_bdd.json")
    private Resource resource;

    @Override
    public List<String> generateDemoLaunches(DemoDataRq rq, String user, String projectName) {
        Map<String, Map<String, List<String>>> stories;
        try {
            stories = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
            });
        } catch (IOException e) {
            throw new ReportPortalException("Unable to load stories description. " + e.getMessage(), e);
        }
        return generateLaunches(rq, stories, user, projectName);
    }

    private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> storiesStructure,
                                          String user, String project) {
        return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
            String launchId = startLaunch(NAME + "_" + rq.getPostfix(), i, project, user);

            boolean hasBeforeAfterStories = random.nextBoolean();
            if (hasBeforeAfterStories) {
                finishRootItem(
                        startRootItem("BeforeStories", launchId, STORY).getId());
            }
            generateStories(storiesStructure, i, launchId);
            if (hasBeforeAfterStories) {
                finishRootItem(
                        startRootItem("AfterStories", launchId, STORY).getId());
            }
            finishLaunch(launchId);
            return launchId;
        }).collect(toList());
    }

    private List<String> generateStories(Map<String, Map<String, List<String>>> storiesStructure, int i, String launchId) {
        List<String> stories = storiesStructure.entrySet().stream().limit(i + 1).map(story -> {
            TestItem storyItem = startRootItem(story.getKey(), launchId, STORY);
            story.getValue().entrySet().forEach(scenario -> {
                if (random.nextBoolean()) {
                    finishTestItem(
                            startTestItem(storyItem, launchId, "beforeScenario", SCENARIO).getId(), status(), strategy);
                }
                TestItem scenarioItem = startTestItem(storyItem, launchId, scenario.getKey(), SCENARIO);
                boolean isFailed = false;
                for (String step : scenario.getValue()) {
                    TestItem stepItem = startTestItem(scenarioItem, launchId, step, STEP);
                    String status;
                    if (isFailed) {
                        status = SKIPPED.name();
                    } else {
                        status = status();
                        if (FAILED.name().equalsIgnoreCase(status) || SKIPPED.name().equalsIgnoreCase(status)) {
                            isFailed = true;
                        }
                    }
                    logDemoDataService.generateDemoLogs(stepItem.getId(), status);
                    finishTestItem(stepItem.getId(), status, strategy);
                }
                finishTestItem(scenarioItem.getId(), isFailed ? FAILED.name() : PASSED.name(), strategy);
                if (random.nextBoolean()) {
                    finishTestItem(
                            startTestItem(storyItem, launchId, "afterScenario", SCENARIO).getId(), status(), strategy);
                }
            });
            finishRootItem(storyItem.getId());
            return storyItem.getId();
        }).collect(toList());
        if (random.nextBoolean()) {
            stories.add(generateCustomStory(launchId));
        }
        return stories;
    }

    /**
     * Hardcoded generating a demo story with complex structure.
     *
     * @param launchId
     * @return story id
     */
    private String generateCustomStory(String launchId) {
        TestItem outerStory = startRootItem("Complex story with given inner story", launchId, STORY);
        TestItem innerStory = startTestItem(outerStory, launchId, "Given Story", STORY);
        TestItem innerScenario = startTestItem(innerStory, launchId, "A given story scenario", SCENARIO);
        TestItem innerStep = startTestItem(innerScenario, launchId, "Today has 'a' and 'y' in its name", STEP);
        TestItem outerScenario = startTestItem(outerStory, launchId, "Simple Scenario", SCENARIO);
        TestItem outerStep = startTestItem(outerScenario, launchId, "Simple Step", STEP);
        logDemoDataService.generateDemoLogs(innerStep.getId(), PASSED.name());
        logDemoDataService.generateDemoLogs(outerStep.getId(), FAILED.name());
        finishTestItem(outerStep.getId(), FAILED.name(), strategy);
        finishTestItem(outerScenario.getId(), FAILED.name(), strategy);
        finishTestItem(innerStep.getId(), PASSED.name(), strategy);
        finishTestItem(innerScenario.getId(), PASSED.name(), strategy);
        finishTestItem(innerStory.getId(), PASSED.name(), strategy);
        finishRootItem(outerStory.getId());
        return outerStory.getId();
    }
}
