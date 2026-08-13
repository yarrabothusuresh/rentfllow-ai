package com.rentflow.event;

import com.rentflow.event.dto.CreateEventRequest;
import com.rentflow.event.dto.EventDTO;
import com.rentflow.workflow.model.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventControllerTest {

    private EventRepository eventRepository;
    private EventController eventController;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        eventController = new EventController(eventRepository);
    }

    @Test
    void testCreateEvent() {
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("Test Wedding");
        request.setGuestCount(200);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<EventDTO> response = eventController.createEvent(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Wedding", response.getBody().getEventName());
        assertEquals(200, response.getBody().getGuestCount());
        assertEquals(EventStatus.PLANNING, response.getBody().getStatus());
    }

    @Test
    void testGetAllEvents() {
        UUID tenantId = UUID.randomUUID();
        Event event = new Event(
                UUID.randomUUID(),
                tenantId,
                UUID.randomUUID(),
                "Dallas Gala",
                "Gala",
                LocalDate.now(),
                null,
                null,
                150,
                "Venue A",
                "Dallas, TX",
                "None",
                EventStatus.QUOTED
        );

        when(eventRepository.findByTenantId(tenantId)).thenReturn(List.of(event));

        ResponseEntity<List<EventDTO>> response = eventController.getAllEvents(tenantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Dallas Gala", response.getBody().get(0).getEventName());
    }

    @Test
    void testGetEventById() {
        UUID id = UUID.randomUUID();
        Event event = new Event(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Birthday Bash",
                "Birthday",
                LocalDate.now(),
                null,
                null,
                50,
                "Venue B",
                "Austin, TX",
                "Party favors",
                EventStatus.PLANNING
        );

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        ResponseEntity<EventDTO> response = eventController.getEventById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Birthday Bash", response.getBody().getEventName());
    }
}
