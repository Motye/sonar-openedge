/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.api.Constants;

import com.google.common.io.Files;

public class TestProjectSensorRtbContext {
  public final static String BASEDIR = "target/test-classes/project2";
  public final static String DF1 = "src/schema/sp2k.df";
  public final static String FILE1 = "src/procedures/test1.p";
  public final static String FILE5 = "src/procedures/invalid.p";
  public final static String CLASS1 = "src/classes/rssw/testclass.cls";

  private TestProjectSensorRtbContext() {
    // No-op
  }

  public static SensorContextTester createContext() throws IOException {
    return createContext(new MapSettings());
  }

  public static SensorContextTester createContext(MapSettings settings) throws IOException {
    settings.setProperty("sonar.sources", "src");
    settings.setProperty(Constants.PROPATH, new File(BASEDIR).getAbsolutePath());
    settings.setProperty(Constants.BINARIES, "build");
    settings.setProperty(Constants.DATABASES, "src/schema/sp2k.df");
    settings.setProperty(Constants.SKIP_RCODE, true);
    settings.setProperty(Constants.PROPARSE_ERROR_STACKTRACE, false);
    settings.setProperty(Constants.RTB_COMPATIBILITY, true);

    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, DF1) //
      .setLanguage(Constants.DB_LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.asCharSource(new File(BASEDIR, DF1), Charset.defaultCharset()).read()) //
      .build());
    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, FILE1) //
      .setLanguage(Constants.LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.asCharSource(new File(BASEDIR, FILE1), Charset.defaultCharset()).read()) //
      .build());
    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, FILE5) //
      .setLanguage(Constants.LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.asCharSource(new File(BASEDIR, FILE5), Charset.defaultCharset()).read()) //
      .build());
    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, CLASS1) //
      .setLanguage(Constants.LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.asCharSource(new File(BASEDIR, CLASS1), Charset.defaultCharset()).read()) //
      .build());

    return context;
  }
}