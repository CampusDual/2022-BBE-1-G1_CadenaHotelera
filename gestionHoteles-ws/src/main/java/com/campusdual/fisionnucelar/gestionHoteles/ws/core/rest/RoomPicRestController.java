package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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

import net.sf.jasperreports.engine.JRException;

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
	public @ResponseBody ResponseEntity<?> uploadFileHandler(@RequestParam("name") String name,
			@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {

		if (!file.isEmpty()) {

			byte[] bytes;
			try {
				bytes = file.getBytes();

				Map<String, Object> attrMap = new HashMap<>();
				attrMap.put("rp_room", 1);
				attrMap.put("rp_image", bytes);
				
				roomPicService.roompicInsert(attrMap);

//             // Creating the directory to store file
//             String rootPath = System.getProperty("catalina.home");
//             File dir = new File(rootPath + File.separator + "tmpFiles");
//             if (!dir.exists()) {
//                 dir.mkdirs();
//             }
//
//             // Create the file on server
//             File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
//             BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
//             stream.write(bytes);
//             stream.close();
//
//             System.out.println("Server File Location=" + serverFile.getAbsolutePath());

//             return null;
//         } catch (Exception e) {
//             return null;
//         }

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
		return ResponseEntity.ok(null);
	}

	@GetMapping("/{id_room_pic}")
	public ResponseEntity<byte[]> getReceipt(@PathVariable("id_room_pic") int roomPicId)
			throws OntimizeJEERuntimeException, JRException, IOException, SQLException {
		HttpHeaders headers = new HttpHeaders();
		byte[] contents = null;
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room_pic", roomPicId);

		contents = roomPicService.roompicQuery(keyMap, Arrays.asList("rp_image"));
		headers.setContentType(MediaType.IMAGE_JPEG);
		String filename = "output.pdf";
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		return new ResponseEntity<>(contents, headers, HttpStatus.OK);
	}
}
