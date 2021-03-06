using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class Files
  {
    private static readonly Lazy<string> ourCachedNuGetExe_1_4 = PathSearcher.SearchFile("lib/nuget/1.4/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_5 = PathSearcher.SearchFile("lib/nuget/1.5/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_6 = PathSearcher.SearchFile("lib/nuget/1.6/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetRunnerPath = PathSearcher.SearchFile("JetBrains.TeamCity.NuGetRunner.exe", "bin/JetBrains.TeamCity.NuGetRunner.exe");
    private static readonly Lazy<string> ourLocalFeed = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed");
    private static readonly Lazy<string> ourLocalFeed_1_4 = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed_1.4");
    private static readonly Lazy<string> ourCachedNuGet_CI_Last = new Lazy<string>(() => FetchLatestNuGetPackage("bt4"));
    private static readonly Lazy<string> ourCachedNuGet_CI_16 = new Lazy<string>(() => FetchLatestNuGetPackage("bt28"));
    private static readonly Lazy<string> ourCachedNuGet_CommandLinePackage_Last = new Lazy<string>(FetchLatestNuGetCommandline); 

    public static string GetLocalFeed(NuGetVersion version)
    {
      return version.Is_1_4() ? ourLocalFeed_1_4.Value : ourLocalFeed.Value;
    }
    
    public static string NuGetExe_1_4 { get { return ourCachedNuGetExe_1_4.Value; } }
    public static string NuGetExe_1_5 { get { return ourCachedNuGetExe_1_5.Value; } }
    public static string NuGetExe_1_6 { get { return ourCachedNuGetExe_1_6.Value; } }
    public static string NuGetRunnerExe { get { return ourCachedNuGetRunnerPath.Value; } }
    public static string LocalFeed { get { return ourLocalFeed.Value; } }


    public static string GetNuGetExe(NuGetVersion version)
    {
      switch (version)
      {
        case NuGetVersion.NuGet_1_4:
          return NuGetExe_1_4;
        case NuGetVersion.NuGet_1_5:
          return NuGetExe_1_5;
        case NuGetVersion.NuGet_1_6:
          return NuGetExe_1_6;
        case NuGetVersion.NuGet_Latest_CI:
          return ourCachedNuGet_CI_Last.Value;
        case NuGetVersion.NuGet_16_CI:
          return ourCachedNuGet_CI_16.Value;
        case NuGetVersion.NuGet_CommandLine_Package_Latest:
          return ourCachedNuGet_CommandLinePackage_Last.Value;
        default:
          throw new Exception("Unsupported nuget version: " + version);
      }
    }

    private static string FetchLatestNuGetPackage(string bt)
    {
      var homePath = CreateTempPath();
      //TODO: better is to download entire nuget.commandline package
      string url = "http://ci.nuget.org:8080/guestAuth/repository/download/" + bt + "/.lastSuccessful/Console/NuGet.exe";
      var nugetPath = Path.Combine(homePath, "NuGet.exe");
      var cli = new WebClient();
      cli.DownloadFile(url, nugetPath);
      return nugetPath;
    }

    private static string CreateTempPath()
    {
      var homePath = Path.GetTempFileName();
      File.Delete(homePath);
      Directory.CreateDirectory(homePath);
      return homePath;
    }

    private static string FetchLatestNuGetCommandline()
    {
      var temp = CreateTempPath();
      ProcessExecutor.ExecuteProcess(NuGetExe_1_5, "install", "NuGet.commandline", "-ExcludeVersion", "-OutputDirectory",
                                     temp).Dump().AssertNoErrorOutput().AssertExitedSuccessfully();
      string nugetPath = Path.Combine(temp, "NuGet.CommandLine/tools/NuGet.Exe");
      Assert.IsTrue(File.Exists(nugetPath));
      return nugetPath;
    }

    public static IEnumerable<string> GetNuGetPackageFiles()
    {
      return Directory.GetFiles(ourLocalFeed.Value, "*.nupkg");
    }

    public static string WebPackage_1_1_1
    {
      get { return Path.Combine(ourLocalFeed.Value, "Web.1.1.1.nupkg"); }
    }
  }

  public enum NuGetVersion
  {
    NuGet_1_4,
    NuGet_1_5,
    NuGet_1_6,

    NuGet_Latest_CI,
    NuGet_16_CI,
    NuGet_CommandLine_Package_Latest
  }


  public static class NuGetVersionExtensions
  {
    public static bool Is_1_4(this NuGetVersion version)
    {
      return version == NuGetVersion.NuGet_1_4;
    }
  }

}