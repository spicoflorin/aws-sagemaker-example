package ro.edu.aws.sgm.inference.pmml.randomforest.entrypoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.transform.sax.SAXSource;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.MiningModelEvaluator;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ProbabilityClassificationMap;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RestController
public class SGMController {


  private ModelEvaluator<MiningModel> modelEvaluator;



  @PostConstruct
  public void init() {
    try {
      PMML pmml = createPMMLfromFile("iris_rf.pmml");
      modelEvaluator = new MiningModelEvaluator(pmml);
    } catch (SAXException | JAXBException | IOException e) {
      e.printStackTrace();
    }
  }

  @RequestMapping(value = "/ping", method = RequestMethod.GET)
  public String flinkErrorRateHandler() {
    return "OK@" + System.currentTimeMillis();
  }


  @RequestMapping(value = "/invocations", method = RequestMethod.POST)
  public String invoke(HttpServletRequest request) throws IOException {
    return predict(request.getReader().lines(), modelEvaluator);
  }


  private static String predict(Stream<String> inputData,
      ModelEvaluator<MiningModel> modelEvaluator) {


    String returns = inputData.map(dataLine -> {
      Map<FieldName, FieldValue> arguments = readArgumentsFromLine(dataLine, modelEvaluator);
      modelEvaluator.verify();
      Map<FieldName, ?> results = modelEvaluator.evaluate(arguments);
      FieldName targetName = modelEvaluator.getTargetField();
      Object targetValue = results.get(targetName);
      ProbabilityClassificationMap nodeMap = (ProbabilityClassificationMap) targetValue;

      return nodeMap.getResult().toString();
    }).collect(Collectors.joining(System.lineSeparator()));

    return returns;

  }



  private static PMML createPMMLfromFile(String fileName)
      throws SAXException, JAXBException, IOException {

    try (
        InputStream pmmlFile = SGMController.class.getClassLoader().getResourceAsStream(fileName)) {
      String pmmlString = new Scanner(pmmlFile).useDelimiter("\\Z").next();

      InputStream is = new ByteArrayInputStream(pmmlString.getBytes());

      InputSource source = new InputSource(is);
      SAXSource transformedSource = ImportFilter.apply(source);

      return JAXBUtil.unmarshalPMML(transformedSource);
    }
  }


  private static Map<FieldName, FieldValue> readArgumentsFromLine(String line,
      ModelEvaluator<MiningModel> modelEvaluator) {
    Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
    String[] lineArgs = line.split(",");

    if (lineArgs.length != 5)
      return arguments;

    FieldValue sepalLength = modelEvaluator.prepare(new FieldName("Sepal.Length"),
        lineArgs[0].isEmpty() ? 0 : lineArgs[0]);
    FieldValue sepalWidth = modelEvaluator.prepare(new FieldName("Sepal.Width"),
        lineArgs[1].isEmpty() ? 0 : lineArgs[1]);
    FieldValue petalLength = modelEvaluator.prepare(new FieldName("Petal.Length"),
        lineArgs[2].isEmpty() ? 0 : lineArgs[2]);
    FieldValue petalWidth = modelEvaluator.prepare(new FieldName("Petal.Width"),
        lineArgs[3].isEmpty() ? 0 : lineArgs[3]);

    arguments.put(new FieldName("Sepal.Length"), sepalLength);
    arguments.put(new FieldName("Sepal.Width"), sepalWidth);
    arguments.put(new FieldName("Petal.Length"), petalLength);
    arguments.put(new FieldName("Petal.Width"), petalWidth);

    return arguments;
  }
}
