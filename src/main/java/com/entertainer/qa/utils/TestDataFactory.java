package com.entertainer.qa.utils;

import com.entertainer.qa.models.booker.Booking;
import com.entertainer.qa.models.booker.BookingDates;
import com.entertainer.qa.models.jsonplaceholder.Post;
import com.entertainer.qa.models.reqres.CreateUserRequest;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique-enough request payloads for create/update tests, so parallel runs and repeat
 * executions against the shared public sandboxes don't collide on fixed fixture values.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static CreateUserRequest randomUser() {
        return new CreateUserRequest("qa-user-" + uniqueSuffix(), "SDET");
    }

    public static CreateUserRequest userWith(String namePrefix, String job) {
        return new CreateUserRequest(namePrefix + "-" + uniqueSuffix(), job);
    }

    public static Booking randomBooking() {
        int nights = ThreadLocalRandom.current().nextInt(1, 14);
        LocalDate checkin = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 30));
        LocalDate checkout = checkin.plusDays(nights);

        return Booking.builder()
                .firstname("Midhat-" + uniqueSuffix())
                .lastname("Chughtai")
                .totalprice(ThreadLocalRandom.current().nextInt(50, 1000))
                .depositpaid(ThreadLocalRandom.current().nextBoolean())
                .bookingdates(new BookingDates(checkin.toString(), checkout.toString()))
                .additionalneeds("Breakfast")
                .build();
    }

    public static Booking bookingWithoutAdditionalNeeds() {
        Booking booking = randomBooking();
        booking.setAdditionalneeds(null);
        return booking;
    }

    public static Post randomPost(int userId) {
        return new Post(userId, "QA automation title " + uniqueSuffix(), "QA automation body " + uniqueSuffix());
    }
}
