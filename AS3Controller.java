package com.rbs.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.rbs.service.AS3ClientService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Vishnu Garg
 *
 *         19-Dec-2019
 */
@Component
@RestController
@RequestMapping("/as3/v1")
@Api(value = "AS3 Artifact System", description = "Operations pertaining to Artifact Management System")
public class AS3Controller {

	private static final Logger logger = LoggerFactory.getLogger(AS3Controller.class);

	@Autowired
	private AS3ClientService aS3ClientService;

	@PostMapping("/uploadFile")
	@ApiOperation(value = "upload available Repository")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully repository uploaded"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public Map<String, String> uploadFile(@RequestPart(value = "file") MultipartFile file) {
		this.aS3ClientService.uploadFileToS3Bucket(file, true);

		Map<String, String> response = new HashMap<>();
		response.put("message", "file [" + file.getOriginalFilename() + "] uploading request submitted successfully.");

		return response;
	}

	@DeleteMapping("/deleteFile")
	@ApiOperation(value = "Delete available Repositories")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully deleted repository"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public Map<String, String> deleteFile(@RequestParam("file_name") String fileName) {
		this.aS3ClientService.deleteFileFromS3Bucket(fileName);

		Map<String, String> response = new HashMap<>();
		response.put("message", "file [" + fileName + "] removing request submitted successfully.");

		return response;
	}

	@ApiOperation(value = "Search a list of available Repositories")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved repositories"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	@GetMapping("/search/repositories")
	public ResponseEntity<ListObjectsV2Result> findContentByRepository(@RequestHeader HttpHeaders headers,
			@ApiParam(value = "Bucket Name from which object will retrieve", required = true) @PathVariable(value = "bucketName") String bucketName,
			@RequestParam("delimiter") String delimiter, @RequestParam("prefix") String prefix) {

		// retrieve the authorization token
		// String authToken = headers.getFirst(HttpHeaders.AUTHORIZATION);
		ListObjectsV2Result result = aS3ClientService.listObjects(bucketName, delimiter, prefix);
		HttpHeaders responseHeaders = new HttpHeaders();
		return new ResponseEntity<ListObjectsV2Result>(result, responseHeaders, HttpStatus.OK);

	}

	@GetMapping("/{userId}/{repoId}")
	public ResponseEntity<String> getContentList(@PathVariable("userId") String userId,
			@PathVariable("repoId") String repoId) {
		return aS3ClientService.getContentList(userId, repoId);
	}

}
