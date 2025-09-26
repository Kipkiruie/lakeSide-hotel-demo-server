package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.exception.PhotoRetrievalException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.response.BookingResponse;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import com.dailycodework.lakesidehotel.service.BookingService;
import com.dailycodework.lakesidehotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;
    private final BookingService bookingService;

    // -------------------- ADD ROOM --------------------
    @PostMapping("/add/new-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }

    // -------------------- GET ROOM TYPES --------------------
    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    // -------------------- GET ALL ROOMS --------------------
    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            RoomResponse roomResponse = getRoomResponse(room);
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                roomResponse.setPhoto(base64Photo);
            }
            roomResponses.add(roomResponse);
        }

        return ResponseEntity.ok(roomResponses);
    }

    // -------------------- DELETE ROOM --------------------
    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // -------------------- UPDATE ROOM --------------------
    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws SQLException, IOException {

        Room updatedRoom = roomService.updateRoom(roomId, roomType, roomPrice, photo);
        RoomResponse roomResponse = getRoomResponse(updatedRoom);
        return ResponseEntity.ok(roomResponse);
    }

    // -------------------- GET ROOM BY ID --------------------
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> ResponseEntity.ok(Optional.of(getRoomResponse(room))))
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    // -------------------- GET AVAILABLE ROOMS --------------------
    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam("roomType") String roomType
    ) throws SQLException {

        List<Room> availableRooms = roomService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : availableRooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            RoomResponse roomResponse = getRoomResponse(room);
            if (photoBytes != null && photoBytes.length > 0) {
                String photoBase64 = Base64.encodeBase64String(photoBytes);
                roomResponse.setPhoto(photoBase64);
            }
            roomResponses.add(roomResponse);
        }

        if (roomResponses.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(roomResponses);
        }
    }

    // -------------------- PRIVATE HELPERS --------------------
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = bookingService.getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmationCode()))
                .toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes,
                bookingInfo
        );
    }
}
