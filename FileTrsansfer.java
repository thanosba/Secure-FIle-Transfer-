

/**
 *
 * @author thanosbalas
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

public class FileTrsansfer {
private Socket socket;
	private static final int MAX_BUFFER = 8192;

	public FileTrsansfer(Socket socket) {
		this.socket = socket;
	}

	public boolean sendFile(File file) {

		boolean errorOnSave = false;
		long length = file.length();

		if (file.exists()) {

			FileInputStream in = null;
			DataOutputStream out = null;

			try {
				in = new FileInputStream(file);
				out = new DataOutputStream(this.socket.getOutputStream());

				out.writeLong(length);
				out.flush();

				byte buffer[] = new byte[MAX_BUFFER];
				int read = 0;

				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
					out.flush();
					buffer = new byte[MAX_BUFFER];
				}

			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				return false;
			} catch (IOException e) {
				System.out.println("An error has occurred when try send file " + file.getName() + " \nSocket: "
						+ socket.getInetAddress() + ":" + socket.getPort() + "\n\t" + e.getMessage());
				errorOnSave = true;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						System.out.println("An error has occurred when closing the InputStream of the file "
								+ file.getName() + "\n\t" + e.getMessage());
					}
				}

			}
			return !errorOnSave;
		} else {
			return false;
		}
	}

	public boolean saveFile(File fileSave) {
		RandomAccessFile file = null;
		DataInputStream in = null;

		boolean errorOnSave = false;
		try {
			file = new RandomAccessFile(fileSave, "rw");

			file.getChannel().lock();

			in = new DataInputStream(this.socket.getInputStream());
			long fileSize = in.readLong();

			byte buffer[] = new byte[MAX_BUFFER];
			int read = 0;

			while ((fileSize > 0) && ((read = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1)) {
				file.write(buffer, 0, read);
				fileSize -= read;
				buffer = new byte[MAX_BUFFER];
			}

		} catch (FileNotFoundException e1) {
			System.out.println(e1.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("An error has occurred when saving the file\n\t" + e.getMessage());
			errorOnSave = true;
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					System.out.println(
							"An error occurred when closing the file " + fileSave.getName() + "\n\t" + e.getMessage());
					errorOnSave = true;
				}
			}
			if (errorOnSave) {
				if (fileSave.exists()) {
					fileSave.delete();
				}
			}

		}
		return !errorOnSave;
	}

}
