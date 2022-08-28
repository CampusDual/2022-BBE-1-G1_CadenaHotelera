package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomPicService;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This class listens the incoming requests related with the room pics table
 * 
 * @since 22/08/2022
 * @version 1.0
 *
 */
@RestController
@RequestMapping("/roompics")
public class RoomPicRestController {

	@Autowired
	private IRoomPicService roomPicService;

	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> uploadFileHandler(@RequestParam("rp_hotel") int hotel,
			@RequestParam("rp_room_type") int roomType, @RequestParam("file") MultipartFile file,
			HttpServletRequest request, HttpServletResponse response) {

		if (!file.isEmpty()) {
			byte[] bytes;
			try {
				bytes = file.getBytes();
				Map<String, Object> attrMap = new HashMap<>();
				attrMap.put("rp_room_type", roomType);
				attrMap.put("rp_hotel", hotel);
				attrMap.put("rp_image", bytes);
				roomPicService.roompicInsert(attrMap);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(null);
	}

	@GetMapping("/{id_room_pic}")
	public ResponseEntity<byte[]> getImage(@PathVariable("id_room_pic") int roomPicId)
			throws OntimizeJEERuntimeException {
		HttpHeaders headers = new HttpHeaders();
		byte[] contents = null;
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room_pic", roomPicId);
		contents = roomPicService.roompicQuery(keyMap, Arrays.asList("rp_image"));

		headers.setContentType(MediaType.IMAGE_JPEG);
		String filename = "image.jpg";
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		return new ResponseEntity<>(contents, headers, HttpStatus.OK);
	}

	@GetMapping()
	public ResponseEntity<EntityResult> getRoomTypeImages(@RequestParam("rp_hotel") Integer hotel,
			@RequestParam(value = "rp_room_type", required = false) Integer roomType)
			throws OntimizeJEERuntimeException {
		HttpHeaders headers = new HttpHeaders();
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("rp_hotel", hotel);
		if (!(roomType == null)) {
			keyMap.put("rp_room_type", roomType);
		}
		EntityResult result = roomPicService.roomtypepicsQuery(keyMap, Arrays.asList("rp_image"));

		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

}
