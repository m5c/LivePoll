package com.github.m5c.livepoll.persistence;

import com.github.m5c.livepoll.Pack;
import com.github.m5c.livepoll.PackMeta;
import com.github.m5c.livepoll.pollutils.Hyphenizer;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper component to save and load pack files from disk.
 */
@Component
public class PackPersistence {

  private final String baseDir;

  /**
   * Constructor for the PackPersitence component. A folder named "packs" must be within th provided
   * location, for persistence to work. Note that values starting without slash are considered
   * locations relative to the user's home sirectory.
   *
   * @param baseDir as the directory to use as base path for configurations and persistence.
   */
  public PackPersistence(@Value("${base.dir}") String baseDir) {

    // If baseDir variable in properties file has no leading "/" we assume it is a location
    // realtive to the homedir.
    if (baseDir.startsWith("/")) {
      this.baseDir = baseDir;
    } else {
      this.baseDir = System.getProperty("user.home") + "/" + baseDir;
    }

  }

  /**
   * Helper method to store a provided pack onject on disk. The object is serialized to json and
   * stored in the configured application basedir (see application.porperties).
   *
   * @param pack as the pack object to save to disk.
   * @return String containing absolute path to persisted file on disk.
   * @throws IOException in case of a filesystem access error.
   */
  public String persistPackToDisk(Pack pack) throws IOException {

    // Serialize pack
    String serializedPack = new Gson().toJson(pack);

    // Create full path and filename (relative location in basedir, hyphenized name, json suffix)
    File samplePackFile = new File(
        baseDir + "/packs/" + pack.getMeta().getCreation() + "-" + Hyphenizer.hyphenize(
            pack.getMeta().getTitle()) + ".json");

    // Save to disk
    FileUtils.writeStringToFile(samplePackFile, serializedPack);
    return samplePackFile.getAbsolutePath();
  }

  /**
   * Searches the configured basedir for persisted packs and creates an indexed map for each. The
   * map translates from PackMeta object to the filename of the associated pack.
   *
   * @return Map of all PackMetas filenames to the correpsonding Pack meta object.
   * @throws IOException in case of a files system access error on load.
   */
  public Map<String, PackMeta> loadAllPackMetas() throws IOException {

    // Preapre result map. Will be filled further down in this method.
    Map<String, PackMeta> packMetas = new LinkedHashMap<>();

    // Create file for dir on disk with all packs
    File packDir = new File(baseDir + "/packs");

    // Iterate non-recursivley over all JSON files in pack directory.
    Iterator<File> packFileIterator = FileUtils.iterateFiles(packDir, new String[] {"json"}, false);
    while (packFileIterator.hasNext()) {
      // load pack, extract meta object
      String packFileName = packFileIterator.next().getName();

      //add entry to result map;
      packMetas.put(packFileName, loadPackFromDisk(packFileName).getMeta());
    }
    return packMetas;
  }

  /**
   * Loads a pack object from disk. Searches the packs subdirectory of the basedir configured in
   * application.properties.
   *
   * @param fileName as the filename with file-extension of the pack (no full path, just the name).
   * @return deserialized pack object.
   * @throws IOException if the requested file does not exist.
   */
  public Pack loadPackFromDisk(String fileName) throws IOException {
    // Resolve full file path
    File targetPackFile = new File(baseDir + "/packs/" + fileName);

    // Load json string
    String packAsJson = FileUtils.readFileToString(targetPackFile);

    // Convert back to object and return
    return new Gson().fromJson(packAsJson, Pack.class);
  }

  /**
   * Deletes a previously persisted pack object. Includes basic verification to ensure the
   * referenced file actually is a pack.
   *
   * @param testPackDiskLocation as absolute path string referencing file to delete.
   * @throws PackPersistenceException in case the pack requested for deletion cannot be found.
   */
  public void deletePack(String testPackDiskLocation) throws PackPersistenceException {

    // Verify the referenced file is a pack
    // Suffix must be json
    if (!testPackDiskLocation.endsWith("json")) {
      throw new PackPersistenceException("Provided file is not a pack file.");
    }
    // parent folder must be "packs"
    File packFile = new File(testPackDiskLocation);
    if (!packFile.exists()) {
      throw new PackPersistenceException("Pack file cannot be deleted, because it does not exist.");
    }
    if (!packFile.getParent().endsWith("packs")) {
      throw new PackPersistenceException(
          "Provided file is not in valid pack location: " + packFile.getAbsolutePath());
    }
    packFile.delete();
  }
}