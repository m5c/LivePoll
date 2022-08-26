package eu.kartoffelquadrat.livepoll.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import eu.kartoffelquadrat.livepoll.Poll;
import eu.kartoffelquadrat.livepoll.PollManager;
import eu.kartoffelquadrat.livepoll.pollutils.Hyphenizer;
import eu.kartoffelquadrat.livepoll.qrgenerator.LocalIpResolver;
import eu.kartoffelquadrat.livepoll.qrgenerator.LocalResourceEncoder;
import eu.kartoffelquadrat.livepoll.qrgenerator.QrImageGenerator;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to register votes caused by scannign of QR codes.
 */
@RestController
public class PollController {

  QrImageGenerator qrImageGenerator;

  LocalResourceEncoder localResourceEncoder;

  LocalIpResolver localIpResolver;

  PollManager pollManager;

  /**
   * Bean constructor.
   *
   * @param qrImageGenerator     as the generator to be used for the creation of QR BitMatrix
   *                             objects from resource strings.
   * @param localResourceEncoder as the encoder to be used to generate URI strings for vote option
   *                             resources.
   * @param localIpResolver      asa helper tool to look up the own LAN IP address of this
   *                             webservice.
   * @param pollManager          as the manager object that indexes all active polls.
   */
  @Autowired
  public PollController(QrImageGenerator qrImageGenerator,
                        LocalResourceEncoder localResourceEncoder,
                        LocalIpResolver localIpResolver,
                        PollManager pollManager) {
    this.qrImageGenerator = qrImageGenerator;
    this.localResourceEncoder = localResourceEncoder;
    this.localIpResolver = localIpResolver;
    this.pollManager = pollManager;
  }

  /**
   * REST endpoint to create a new poll, based on the options provided as body payload.   *
   *
   * @return id of the newly created poll.
   * @throws IOException     in case the implicit lookup of the pwn webapps LAN ip failed.
   * @throws WriterException in case the writing of a QR png file to the file system failed.
   */

  @PostMapping("/polls")
  public String createPoll(@RequestBody Poll poll) throws IOException, WriterException {

    String pollId = pollManager.addPoll(poll);
    createPollQrCodes(pollId, poll);
    return pollId;
  }

  /**
   * An actual endpoint, referenced by generated QR code. Note using Get operation here is a clear
   * violation to the REST style, but since we want to support vote by QR scanning it has to be GET
   * (default HTTP method for browser resource access).
   */
  @GetMapping("/polls/{pollid}/{vote}")
  public String registerVote(@PathVariable("vote") String vote) {
    return "I registered your vote \"" + vote + "\". Thank you for your participation.";
  }

  /**
   * Private helper method to generate the qrcodes for a poll option and store the qr files on
   * disk.
   *
   * @param poll object for which qrcodes are required. No path information is required since all
   *             qrcode names consist of poll identifier+option in kebap notation.
   * @throws IOException     lookup of qr target IP fails
   * @throws WriterException if writing of qrcode to disk fails
   */
  private void createPollQrCodes(String pollId, Poll poll)
      throws IOException, WriterException {

    // Create QR code for every option mentioned in poll
    for (String option : poll.getOptions()) {

      // convert option to kebap notation
      String optionResource = Hyphenizer.hyphenize(option);

      // Generate QR code and store on disk
      String resourceString = localResourceEncoder.buildResourceString(pollId, optionResource);
      BitMatrix qrMatrix = qrImageGenerator.encodeQr(resourceString);
      String qrFileName = pollId + "-" + optionResource;
      qrImageGenerator.exportQrToDisk(qrFileName, qrMatrix);
    }
  }
}
