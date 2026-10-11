package com.teammatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teammatch.dto.CreateOpportunityRequest;
import com.teammatch.dto.RoleRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {

    @Test
    public void testValidPayloadDeserializes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = "{\"title\":\"Test\",\"description\":\"Test\",\"type\":\"Project\",\"mode\":\"ONLINE\",\"location\":\"Test\",\"linkType\":\"OTHER\",\"externalLink\":\"\",\"startDate\":\"2024-12-31\",\"endDate\":\"2025-01-31\",\"applicationDeadline\":\"2024-12-30T23:59\",\"roles\":[{\"title\":\"Dev\",\"vacancies\":2,\"requiredSkills\":[]}]}";

        CreateOpportunityRequest request = mapper.readValue(json, CreateOpportunityRequest.class);
        assertNotNull(request);
        assertEquals("Test", request.getTitle());
        assertEquals(1, request.getRoles().size());
        assertEquals(2, request.getRoles().get(0).getVacancies());
    }

    @Test
    public void testVacancyMustBePositive() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // vacancies = 0 should deserialize (validation is done by @Positive annotation at the service layer)
        String json = "{\"title\":\"Test\",\"description\":\"Test\",\"type\":\"Project\",\"mode\":\"ONLINE\",\"location\":\"Test\",\"linkType\":\"OTHER\",\"externalLink\":\"\",\"startDate\":\"2024-12-31\",\"endDate\":\"2025-01-31\",\"applicationDeadline\":\"2024-12-30T23:59\",\"roles\":[{\"title\":\"Dev\",\"vacancies\":0,\"requiredSkills\":[]}]}";

        CreateOpportunityRequest request = mapper.readValue(json, CreateOpportunityRequest.class);
        assertNotNull(request);
        assertEquals(0, request.getRoles().get(0).getVacancies());
    }

    @Test
    public void testInvalidLinkTypeThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = "{\"title\":\"Test\",\"description\":\"Test\",\"type\":\"Project\",\"mode\":\"ONLINE\",\"location\":\"Test\",\"linkType\":\"GITHUB\",\"externalLink\":\"\",\"startDate\":\"2024-12-31\",\"endDate\":\"2025-01-31\",\"applicationDeadline\":\"2024-12-30T23:59\",\"roles\":[{\"title\":\"Dev\",\"vacancies\":1,\"requiredSkills\":[]}]}";

        assertThrows(Exception.class, () -> mapper.readValue(json, CreateOpportunityRequest.class));
    }
}
