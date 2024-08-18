package net.glenmazza.marketoclient.service;

import net.glenmazza.marketoclient.TestApplication;
import net.glenmazza.marketoclient.model.activities.ActivityTypeResponse;
import net.glenmazza.marketoclient.model.activities.ActivityTypeResponse.ActivityType;
import net.glenmazza.marketoclient.model.activities.ActivityTypeResponse.ActivityTypeAttribute;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("testing requires config setup in application-test.properties")
public class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    // may need to adjust test values below based on your Marketo environment
    @Test
    public void testGetActivityTypes() {
        ActivityTypeResponse response = activityService.getActivityTypes();
        assertTrue(response.isSuccess());
        assertFalse(response.isMoreResult());
        assertNull(response.getNextPageToken());
        assertEquals(86, response.getResult().size());

        ActivityType activityType = response.getResult().stream()
                .filter(at -> "Open Email".equals(at.getName())).findFirst().orElse(null);
        assertNotNull(activityType);
        assertEquals(10, activityType.getId());
        assertEquals("User opens Marketo Email", activityType.getDescription());

        ActivityTypeAttribute primaryAttribute = activityType.getPrimaryAttribute();
        assertEquals("Mailing ID", primaryAttribute.getName());
        assertEquals("integer", primaryAttribute.getDataType());
        assertEquals(11, activityType.getAttributes().size());

        ActivityTypeAttribute activityTypeAttribute = activityType.getAttributes().stream()
                .filter(att -> "Is Mobile Device".equals(att.getName())).findFirst().orElse(null);
        assertNotNull(activityTypeAttribute);
        assertEquals("boolean", activityTypeAttribute.getDataType());
    }

}
