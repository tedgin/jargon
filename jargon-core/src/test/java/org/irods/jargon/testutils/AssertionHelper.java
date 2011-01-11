/**
 *
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.GENERATED_FILE_DIRECTORY_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;

/**
 * Helpful assertions for unit testing IRODS
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since
 * 
 */
public class AssertionHelper {
	private Properties testingProperties = new Properties();
	private TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private ScratchFileUtils scratchFileUtils = null;
	private static final String ASSERTION_ERROR_MESSAGE = "assertion failed -- ";
	private static final String FILE_DOES_NOT_EXIST_ERROR = "requested file does not exist!";

	public AssertionHelper() throws TestingUtilsException {
		testingProperties = testingPropertiesHelper.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
	}

	/**
	 * Ensures that a scratch file does not exist given the path/file name
	 * 
	 * @param filePathRelativeToScratch
	 *            <code>String</code> that gives the relative file path under
	 *            scratch, with no leading separator character
	 * @throws IRODSTestAssertionException
	 */
	public void assertLocalFileNotExistsInScratch(
			final String filePathRelativeToScratch)
			throws IRODSTestAssertionException {
		StringBuilder fullPathToLocalFile = computeFullPathToLocalFile(filePathRelativeToScratch);
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append(ASSERTION_ERROR_MESSAGE);
		errorMessage.append("local file exists and should not");
		errorMessage.append(fullPathToLocalFile);
		File localFile = new File(fullPathToLocalFile.toString());
		if (localFile.exists()) {
			throw new IRODSTestAssertionException(errorMessage.toString());
		}

	}

	/**
	 * Ensures that a file exists given the path/file name
	 * 
	 * @param filePathRelativeToScratch
	 *            <code>String</code> that gives the relative file path under
	 *            scratch, with no leading separator character
	 * @throws IRODSTestAssertionException
	 */
	public void assertLocalFileExistsInScratch(
			final String filePathRelativeToScratch)
			throws IRODSTestAssertionException {
		StringBuilder fullPathToLocalFile = computeFullPathToLocalFile(filePathRelativeToScratch);
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append(ASSERTION_ERROR_MESSAGE);
		errorMessage.append("local file does not exist:");
		errorMessage.append(fullPathToLocalFile);
		File localFile = new File(fullPathToLocalFile.toString());
		if (!localFile.exists()) {
			throw new IRODSTestAssertionException(errorMessage.toString());
		}

	}

	/**
	 * Ensures that the given file has the expected length
	 * 
	 * @param filePathRelativeToScratch
	 *            <code>String</code> that gives the relative file path under
	 *            scratch, with no leading separator character
	 * @param expectedLength
	 *            <code>long</code> with length in KB of file that is expected
	 * @throws IRODSTestAssertionException
	 */
	public void assertLocalScratchFileLengthEquals(
			final String filePathRelativeToScratch, final long expectedLength)
			throws IRODSTestAssertionException {
		StringBuilder fullPathToLocalFile = computeFullPathToLocalFile(filePathRelativeToScratch);
		File localFile = new File(fullPathToLocalFile.toString());
		if (!localFile.exists()) {
			throw new IRODSTestAssertionException(FILE_DOES_NOT_EXIST_ERROR);
		}
		if (localFile.length() != expectedLength) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append(ASSERTION_ERROR_MESSAGE);
			errorMessage.append("file length error, expected:");
			errorMessage.append(expectedLength);
			errorMessage.append(" actual:");
			errorMessage.append(localFile.length());
			errorMessage.append(" for file:");
			errorMessage.append(fullPathToLocalFile);
			throw new IRODSTestAssertionException(errorMessage.toString());
		}
	}

	/**
	 * Ensure that the given local file exists and has the expected checksum
	 * value
	 * 
	 * @param filePathRelativeToScratch
	 *            <code>String</code> that gives the relative file path under
	 *            scratch, with no leading separator character
	 * @param actualChecksum2
	 *            <code>long</code> value with the anticipated MD5 checksum
	 * @throws IRODSTestAssertionException
	 */
	public void assertLocalFileHasChecksum(
			final String filePathRelativeToScratch,
			final byte[] expectedChecksum) throws IRODSTestAssertionException {
		byte[] actualChecksum;

		try {
			actualChecksum = scratchFileUtils
					.computeFileCheckSum(filePathRelativeToScratch);
			boolean areEqual = Arrays.equals(actualChecksum, expectedChecksum);
			if (!areEqual) {
				StringBuilder errorMessage = new StringBuilder();
				errorMessage.append(ASSERTION_ERROR_MESSAGE);
				errorMessage.append("checksum error, expected:");
				errorMessage.append(String.valueOf(expectedChecksum));
				errorMessage.append(" actual:");
				errorMessage.append(String.valueOf(actualChecksum));
				errorMessage.append(" for file:");
				errorMessage.append(filePathRelativeToScratch);
				throw new IRODSTestAssertionException(errorMessage.toString());

			}
		} catch (TestingUtilsException e) {
			StringBuilder message = new StringBuilder();
			message.append("error when computing checksum on file:");
			message.append(filePathRelativeToScratch);
			throw new IRODSTestAssertionException(message.toString(), e);
		}
	}

	protected StringBuilder computeFullPathToLocalFile(
			final String filePathRelativeToScratch) {
		StringBuilder fullPathToLocalFile = new StringBuilder();
		fullPathToLocalFile.append(testingProperties
				.get(GENERATED_FILE_DIRECTORY_KEY));
		fullPathToLocalFile.append(filePathRelativeToScratch);
		return fullPathToLocalFile;
	}

	public void assertIrodsFileMatchesLocalFileChecksum(
			final String absoluteIRODSPathUnderScratch,
			final String absoluteLocalFileUnderScratch)
			throws IRODSTestAssertionException {

		// FIXME: need to update for jargon-core
	}

	/**
	 * Make sure that a file or collection is in IRODS
	 * 
	 * @param absoluteIrodsPathUnderScratch
	 *            <code>String</code> with absolute path (leading '/', or a path
	 *            and filename to look for
	 * @throws IRODSTestAssertionException
	 */
	public void assertIrodsFileOrCollectionExists(
			final String absoluteIrodsPathUnderScratch)
			throws IRODSTestAssertionException {
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(absoluteIrodsPathUnderScratch);
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		try {
			String result = invoker
					.invokeCommandAndGetResultAsString(ilsCommand);
			if (result.indexOf(absoluteIrodsPathUnderScratch) == -1) {

				StringBuilder errorMessage = new StringBuilder();
				errorMessage.append(ASSERTION_ERROR_MESSAGE);
				errorMessage
						.append("assert file or collection exists error, expected to find:");
				errorMessage.append(absoluteIrodsPathUnderScratch);
				throw new IRODSTestAssertionException(errorMessage.toString());
			}

		} catch (IcommandException ice) {
			StringBuilder message = new StringBuilder();
			message.append("error ocurred processing assertion on ils path:");
			message.append(absoluteIrodsPathUnderScratch);
			throw new IRODSTestAssertionException(message.toString(), ice);
		}

	}

	/**
	 * Make sure that a file or collection is not in IRODS
	 * 
	 * @param relativeIrodsPathUnderScratch
	 *            <code>String</code> with relative path (no leading '/', or a
	 *            path and filename to look for
	 * @throws IRODSTestAssertionException
	 * 
	 *             FIXME: does not work for files, need to have a sep method
	 *             that gets the parent collection and searches within that...
	 * 
	 */
	public void assertIrodsFileOrCollectionDoesNotExist(
			final String relativeIrodsPathUnderScratch)
			throws IRODSTestAssertionException {
		IlsCommand ilsCommand = new IlsCommand();
		ilsCommand.setIlsBasePath(relativeIrodsPathUnderScratch);
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		try {
			String result = invoker
					.invokeCommandAndGetResultAsString(ilsCommand);
			if (result.indexOf(relativeIrodsPathUnderScratch) != -1) {

				StringBuilder errorMessage = new StringBuilder();
				errorMessage.append(ASSERTION_ERROR_MESSAGE);
				errorMessage
						.append("assert file/collection does not exist in irods, did not expect to find:");
				errorMessage.append(relativeIrodsPathUnderScratch);
				throw new IRODSTestAssertionException(errorMessage.toString());
			}

		} catch (IcommandException ice) {
			// an exception due to a not exists is actually ok
			if (ice.getMessage().indexOf("does not exist") > -1) {
				// just fine, what I want
			} else {

				StringBuilder message = new StringBuilder();
				message.append("error ocurred processing assertion on ils path:");
				message.append(relativeIrodsPathUnderScratch);
				throw new IRODSTestAssertionException(message.toString(), ice);
			}
		}

	}

	/**
	 * Are two directory trees equal? Take two absolute paths to the local file
	 * system, recursively walk each tree and compare length, file name, and
	 * number of subdirectories/files.
	 * 
	 * @param dir1
	 *            <code>String<code> with the absolute path to a directory
	 * @param dir2
	 *            <code>String<code> with
	 * @throws IRODSTestAssertionException
	 */
	public void assertLocalDirectoriesHaveSameData(final String dir1,
			final String dir2) throws IRODSTestAssertionException {
		File file1 = new File(dir1);
		File file2 = new File(dir2);

		if (file1.exists() && file1.isDirectory()) {
			// ok
		} else {
			throw new IRODSTestAssertionException(
					"the first specified directory does not exist, or is not a directory");
		}

		if (file2.exists() && file2.isDirectory()) {
			// ok
		} else {
			throw new IRODSTestAssertionException(
					"the second specified directory does not exist, or is not a directory");
		}

		// side by side comparison

		File[] file1Files = file1.listFiles();
		File[] file2Files = file2.listFiles();

		if (file1Files.length != file2Files.length) {
			throw new IRODSTestAssertionException(
					"mismatch of number of files in a directory, file1 has:"
							+ file1Files.length + " files, while file2 has:"
							+ file2Files.length);
		}

		for (int i = 0; i < file1Files.length; i++) {
			assertTwoFilesAreEqualByRecursiveTreeComparison(file1Files[i],
					file2Files[i]);
		}

	}

	/**
	 * Recursively match two files/directories for length, number of members,
	 * and name
	 * 
	 * @param file1
	 *            <code>File</code> with a file or directory
	 * @param file2
	 *            <code>File<code> with a file or directory
	 * @throws IRODSTestAssertionException
	 */
	public void assertTwoFilesAreEqualByRecursiveTreeComparison(
			final File file1, final File file2)
			throws IRODSTestAssertionException {

		if (file1.isDirectory() && file2.isDirectory()) {
			File[] file1Files = file1.listFiles();
			File[] file2Files = file2.listFiles();

			if (file1Files.length != file2Files.length) {
				throw new IRODSTestAssertionException(
						"directories differ in the number of files contained, dir1 has "
								+ file1Files.length + " while dir2 has "
								+ file2Files.length);
			}

			for (int i = 0; i < file1Files.length; i++) {
				assertTwoFilesAreEqualByRecursiveTreeComparison(file1Files[i],
						file2Files[i]);
			}

		} else if (file1.isFile() && file2.isFile()) {
			if (file1.length() != file2.length()) {
				throw new IRODSTestAssertionException(
						"file lengths differ, file1 has " + file1.length()
								+ " while file2 has " + file2.length());
			}

			if (file1.getName().equals(file2.getName())) {
				// names are equal
			} else {
				throw new IRODSTestAssertionException(
						"file names are different, file1 abs path is:"
								+ file1.getAbsolutePath()
								+ " while file2 abs path is:"
								+ file2.getAbsolutePath());
			}

		} else {
			throw new IRODSTestAssertionException(
					"file mismatch, one is a file, the other is a directory - file1:"
							+ file1.getAbsolutePath() + " file2:"
							+ file2.getAbsolutePath());
		}

	}

}
