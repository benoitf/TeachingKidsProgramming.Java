package org.teachingextensions.logo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

import org.lambda.actions.Action0;
import org.teachingextensions.utils.VirtualProctor;

import com.spun.util.MySystem;
import com.spun.util.ThreadLauncher;
import com.spun.util.ThreadUtils;
import com.spun.util.io.FileUtils;

public class VirtualProctorWeb extends WindowAdapter
{
  private boolean finished = false;
  @Override
  public void windowClosing(WindowEvent event)
  {
    final BufferedImage scaled = ScreenCapture.getScaledImageOf(event.getComponent(), 200, 150);
    ThreadLauncher.launch(new Action0()
    {
      @Override
      public void call()
      {
        sendImageToWeb(scaled);
        finished = true;
      }
    });
  }
  public void sendImageToDisk(BufferedImage image) throws IOException
  {
    String filename = "C:\\temp\\VirtualProctor.png";
    ImageIO.write(image, "png", new File(filename));
    //TestUtils.displayFile(filename);
  }
  @Override
  public void windowClosed(WindowEvent e)
  {
    while (!finished)
    {
      ThreadUtils.sleep(50);
    }
  }
  public void sendImageToWeb(BufferedImage image)
  {
    try
    {
      String urlFormat = "http://virtualproctor-tkp.appspot.com/org.teachingkidsprogramming.virtualproctor.UploadImageRack?fileName=%s.png";
      String name = URLEncoder.encode(VirtualProctor.internals.getName(), "ISO-8859-1");
      URL url = new URL(String.format(urlFormat, name));
      URLConnection connection = url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      OutputStream outputStream = connection.getOutputStream();
      ImageIO.write(image, "png", outputStream);
      outputStream.close();
      String content = FileUtils.readStream((InputStream) connection.getContent());
      MySystem.event(content);
    }
    catch (UnknownHostException e)
    {
      MySystem.event("No internet connection");
    }
    catch (Exception e)
    {
      MySystem.warning(e);
    }
  }
}
