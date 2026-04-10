package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import com.hclhackathon.hotel.service.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reception")
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionController {
	private final RoomInventoryRepository roomInventory;
	private final RoomTypeRepository roomTypes;

	public ReceptionController(RoomInventoryRepository roomInventory, RoomTypeRepository roomTypes) {
		this.roomInventory = roomInventory;
		this.roomTypes = roomTypes;
	}

	@GetMapping("/rooms")
	public ApiResponse<List<RoomAvailabilityView>> rooms(@RequestParam(required = false) UUID hotelId) {
		var allRoomTypes = roomTypes.findAll();
		var roomTypeById = allRoomTypes.stream()
			.filter(rt -> hotelId == null || rt.hotelId.equals(hotelId))
			.collect(java.util.stream.Collectors.toMap(rt -> rt.id, rt -> rt));

		var views = roomInventory.findAll().stream()
			.filter(ri -> roomTypeById.containsKey(ri.roomTypeId))
			.map(ri -> {
				var rt = roomTypeById.get(ri.roomTypeId);
				return new RoomAvailabilityView(
					ri.id.toString(),
					rt.hotelId.toString(),
					ri.roomTypeId.toString(),
					rt.name,
					ri.roomLabel,
					Boolean.TRUE.equals(ri.isActive)
				);
			})
			.toList();

		return ApiResponse.of(views);
	}

	@PatchMapping("/rooms/{roomInventoryId}/availability")
	public ApiResponse<RoomAvailabilityView> updateAvailability(
		@PathVariable UUID roomInventoryId,
		@Valid @RequestBody UpdateAvailabilityRequest request
	) {
		var room = roomInventory.findById(roomInventoryId).orElseThrow(() -> new NotFoundException("Room inventory not found"));
		room.isActive = request.isActive();
		roomInventory.save(room);

		var rt = roomTypes.findById(room.roomTypeId).orElseThrow(() -> new NotFoundException("Room type not found"));
		return ApiResponse.of(new RoomAvailabilityView(
			room.id.toString(),
			rt.hotelId.toString(),
			room.roomTypeId.toString(),
			rt.name,
			room.roomLabel,
			Boolean.TRUE.equals(room.isActive)
		));
	}

	public record UpdateAvailabilityRequest(
		@NotNull Boolean isActive
	) {}

	public record RoomAvailabilityView(
		String roomInventoryId,
		String hotelId,
		String roomTypeId,
		String roomTypeName,
		String roomLabel,
		boolean isActive
	) {}
}
