package eu.kartoffelquadrat.livepoll.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import eu.kartoffelquadrat.livepoll.qrgenerator.LocalIpResolver;
import eu.kartoffelquadrat.livepoll.qrgenerator.LocalResourceEncoder;
import eu.kartoffelquadrat.livepoll.qrgenerator.QrImageGenerator;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to register votes caused by scannign of QR codes.
 */
@RestController
public class PollController {

  QrImageGenerator qrImageGenerator;

  LocalResourceEncoder localResourceEncoder;

  LocalIpResolver localIpResolver;

  /**
   * Bean constructor.
   *
   * @param qrImageGenerator     as the generator to be used for the creation of QR BitMatrix
   *                             objects from resource strings.
   * @param localResourceEncoder as the encoder to be used to generate URI strings for vote option
   *                             resources.
   * @param localIpResolver      asa helper tool to look up the own LAN IP address of this
   *                             webservice.
   */
  @Autowired
  public PollController(QrImageGenerator qrImageGenerator,
                        LocalResourceEncoder localResourceEncoder,
                        LocalIpResolver localIpResolver) {
    this.qrImageGenerator = qrImageGenerator;
    this.localResourceEncoder = localResourceEncoder;
    this.localIpResolver = localIpResolver;
  }

  /**
   * This one should actually be triggered by thymeleaf access on poll / generation of new poll.
   *
   * @return String pointing to QR code file.
   * @throws IOException     in case the implicit lookup of the pwn webapps LAN ip failed.
   * @throws WriterException in case the writing of a QR png file to the file system failed.
   */
  @GetMapping("toto")
  public String toto() throws IOException, WriterException {

    String resourceString = localResourceEncoder.buildResourceString(42, "foo");
    BitMatrix qrMatrix = qrImageGenerator.encodeQr(resourceString);
    return qrImageGenerator.exportQrToDisk("test.png", qrMatrix);
  }

  /**
   * An actual enpoint, referenced by generated QR code.
   */
  @GetMapping("{pollid}/{vote}")
  public String registerVote(@PathVariable("vote") String vote) {
    return "I registered your vote \"" + vote + "\". Thank you for your participation.";
  }
}
