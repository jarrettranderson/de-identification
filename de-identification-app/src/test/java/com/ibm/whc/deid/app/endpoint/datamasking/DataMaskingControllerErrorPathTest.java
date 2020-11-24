/*
 * (C) Copyright IBM Corp. 2016,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.whc.deid.app.endpoint.datamasking;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.whc.deid.ObjectMapperFactory;
import com.ibm.whc.deid.app.endpoint.Application;
import com.ibm.whc.deid.shared.pojo.config.ConfigSchemaType;
import com.ibm.whc.deid.shared.pojo.config.DeidMaskingConfig;
import com.ibm.whc.deid.shared.pojo.config.json.JsonMaskingRule;
import com.ibm.whc.deid.shared.pojo.masking.DataMaskingModel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
// force using a test profile to avoid using any other active profile
// we do not have a real application-test.properties.
@ActiveProfiles(profiles = {"test"})
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class)
public class DataMaskingControllerErrorPathTest {

  private final String basePath = "/api/v1";

  private MockMvc mockMvc;

  private static final Logger log =
      LoggerFactory.getLogger(DataMaskingControllerErrorPathTest.class);

  @Autowired
  private WebApplicationContext wac;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testMaskEmptyInput() throws Exception {
    String noContent = "";
    String emptyObject = "{}";
    log.info(noContent);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(noContent))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string(startsWith("Required request body is missing")));

    log.info(emptyObject);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(emptyObject))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string("no configuration data"));
  }

  @Test
  public void testMaskNullDataListMember() throws Exception {
    String data = null;

    String config = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));

    List<String> inputList = new ArrayList<>();
    inputList.add(data);
    DataMaskingModel dataMaskingModel =
        new DataMaskingModel(config, inputList, ConfigSchemaType.FHIR);
    ObjectMapper mapper = new ObjectMapper();
    String request = mapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string("Invalid input error data[0]"));
  }

  @Test
  public void testMaskNullOrEmptyData() throws Exception {
    String config = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));

    List<String> inputList = null;
    DataMaskingModel dataMaskingModel =
        new DataMaskingModel(config, inputList, ConfigSchemaType.FHIR);
    ObjectMapper mapper = new ObjectMapper();
    String request = mapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string("Invalid input error data"));

    inputList = new ArrayList<>();
    dataMaskingModel = new DataMaskingModel(config, inputList, ConfigSchemaType.FHIR);
    request = mapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string("Invalid input error data"));
  }

  @Test
  public void testMaskNullConfig() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));

    String config = null;

    List<String> inputList = new ArrayList<>();
    inputList.add(data);
    DataMaskingModel dataMaskingModel =
        new DataMaskingModel(config, inputList, ConfigSchemaType.FHIR);
    ObjectMapper mapper = new ObjectMapper();
    String request = mapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string("no configuration data"));
  }

  @Test
  public void testNullSchemaType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    String config = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    ArrayNode dataNode = rootNode.putArray("data");
    dataNode.add(data);
    rootNode.put("config", config);
    rootNode.put("schemaType", (String) null);
    String request = mapper.writeValueAsString(rootNode);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string(startsWith("Invalid input error schemaType")));
  }

  @Test
  public void testInvalidSchemaType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    String config = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    ArrayNode dataNode = rootNode.putArray("data");
    dataNode.add(data);
    rootNode.put("config", config);
    rootNode.put("schemaType", "invalid");
    String request = mapper.writeValueAsString(rootNode);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
        .andExpect(content().string(startsWith("JSON parse error")));
  }

  @Test
  public void testConfigNoJsonSection() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);
    config.setJson(null);
    List<String> dataList = new ArrayList<>();
    dataList.add(data);
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    ObjectMapper mapper = new ObjectMapper();
    String request = mapper.writeValueAsString(dataMaskingModel);

    // message is for schemaType since empty jsonConfig is provided when jsonConfig is null
    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("`json.schemaType` property is missing")));
  }

  @Test
  public void testConfigJsonNoSchemaType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);
    config.getJson().setSchemaType(null);

    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isBadRequest()).andDo(print())
        .andExpect(content().string(containsString("`json.schemaType` property is missing")));
  }

  @Test
  public void testConfigNullMessageTypes() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().setMessageTypes(null);
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `json.messageTypes` must be provided when `json.messageTypeKey` is provided")));
  }

  @Test
  public void testConfigEmptyMessageTypes() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().setMessageTypes(new ArrayList<>());
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `json.messageTypes` must be provided when `json.messageTypeKey` is provided")));
  }

  @Test
  public void testConfigEmptyInMessageType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMessageTypes().add(0, "");
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: value at offset 0 in `json.messageTypes` list is missing")));
  }

  @Test
  public void testConfigNullInMessageType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMessageTypes().add(1, null);
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: value at offset 1 in `json.messageTypes` list is missing")));
  }

  @Test
  public void testConfigWhitespaceInMessageType() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMessageTypes().add(2, "  \t");
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: value at offset 2 in `json.messageTypes` list is missing")));
  }

  @Test
  public void testConfigJsonNullRule() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMaskingRules().add(0, null);
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `rule` is missing from the rule assignment at offset 0 in the list at `json.maskingRules`")));
  }

  @Test
  public void testConfigJsonRuleNameNull() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMaskingRules().add(1, new JsonMaskingRule("/fhir/patient/number", null));
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `rule` is missing from the rule assignment at offset 1 in the list at `json.maskingRules`")));
  }

  @Test
  public void testConfigJsonPathNull() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMaskingRules().add(2, new JsonMaskingRule(null, "HASH"));
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `jsonPath` in the rule assignment at offset 2 in the list at `json.maskingRules` must start with `/`")));
  }

  @Test
  public void testConfigJsonPathBad() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    int offset = config.getJson().getMaskingRules().size();
    config.getJson().getMaskingRules().add(new JsonMaskingRule("", "HASH"));
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "invalid masking configuration: `jsonPath` in the rule assignment at offset " + offset
                + " in the list at `json.maskingRules` must start with `/`")));
  }

  @Test
  public void testConfigRuleAssignmentMismatch() throws Exception {
    String data = new String(Files
        .readAllBytes(Paths.get(getClass().getResource("/masking/data/simple_fhir.json").toURI())));
    List<String> dataList = new ArrayList<>();
    dataList.add(data);

    String configString = new String(Files.readAllBytes(
        Paths.get(getClass().getResource("/config/fhir/masking_config.json").toURI())));
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    DeidMaskingConfig config = objectMapper.readValue(configString, DeidMaskingConfig.class);

    config.getJson().getMaskingRules().get(0).setRule("no_1");
    config.getJson().getMaskingRules().get(1).setRule("");
    config.getJson().getMaskingRules().get(2).setRule("  "); 
    config.getJson().getMaskingRules().add(new JsonMaskingRule("/fhir/bad/rule", "no_last"));
    DataMaskingModel dataMaskingModel = new DataMaskingModel(
        objectMapper.writeValueAsString(config), dataList, ConfigSchemaType.FHIR);
    String request = objectMapper.writeValueAsString(dataMaskingModel);

    log.info(request);
    this.mockMvc
        .perform(post(basePath + "/deidentification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andDo(print()).andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(
            "The JSON masking rule does not refer to a valid rule: no_1. There are 4 invalid rules.")));
  }

}
