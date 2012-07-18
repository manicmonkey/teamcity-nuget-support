/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.source.NuGetNetworkSourceChecker;
import jetbrains.buildServer.nuget.server.trigger.impl.source.NuGetSourceCheckerImpl;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerSettings;
import jetbrains.buildServer.util.TimeService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 17:25
 */
public class NuGetSourceCheckerTest extends BaseTestCase {
  private Mockery m;
  private TimeService myTimeService;
  private PackageCheckerSettings mySettings;
  private NuGetNetworkSourceChecker myCheckerImpl;
  private NuGetSourceCheckerImpl myChecker;

  private long myTime = 1;
  private long mySettingsExpire = 100;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    mySettings = m.mock(PackageCheckerSettings.class);
    myTimeService = m.mock(TimeService.class);
    myCheckerImpl = m.mock(NuGetNetworkSourceChecker.class);
    myChecker = new NuGetSourceCheckerImpl(myTimeService, mySettings, myCheckerImpl);

    m.checking(new Expectations(){{
      allowing(mySettings).getPackageSourceAvailabilityCheckInterval(); will(new CustomAction("ret") {
        public Object invoke(Invocation invocation) throws Throwable {
          return mySettingsExpire;
        }
      });

      allowing(myTimeService).now(); will(returnValue(new CustomAction("ret") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myTime;
        }
      }));
    }});
  }

  @Test
  public void test_empty() {
    myChecker.getAccessiblePackages(Collections.<CheckablePackage>emptyList());
  }



}
