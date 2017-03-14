console.log("Add Ringtone resource to iOS project if needed");

var fs = require('fs'),
    path = require('path');

module.exports = function (context) {
  var xcode = context.requireCordovaModule('xcode');
  var Q = context.requireCordovaModule('q');
  var deferral = new Q.defer();

  if (context.opts.cordova.platforms.indexOf('ios') < 0) {
    throw new Error('This plugin expects the ios platform to exist.');
  }

  var iosFolder = context.opts.cordova.project ? context.opts.cordova.project.root : path.join(context.opts.projectRoot, 'platforms/ios/');

  fs.readdir(iosFolder, function (err, data) {
    if (err) {
      throw err;
    }

    var projFolder;
    var projName;

    // Find the project folder by looking for *.xcodeproj
    if (data && data.length) {
      data.forEach(function (folder) {
        if (folder.match(/\.xcodeproj$/)) {
          projFolder = path.join(iosFolder, folder);
          projName = path.basename(folder, '.xcodeproj');
        }
      });
    }

    if (!projFolder || !projName) {
      throw new Error("Could not find an .xcodeproj folder in: " + iosFolder);
    }

    var destFile = path.join(iosFolder, projName, 'Resources', 'Ringtone.caf');
    if (fs.existsSync(destFile)) {
      console.log("File exists, not doing anything: " + destFile);
      deferral.resolve();
    } else {
      var sourceFile = path.join('resources', 'Ringtone.caf');
      fs.readFile(sourceFile, function (err, data) {
        var resourcesFolderPath = path.join(iosFolder, projName, 'Resources');
        fs.existsSync(resourcesFolderPath) || fs.mkdirSync(resourcesFolderPath);
        fs.writeFileSync(destFile, data);

        var projectPath = path.join(projFolder, 'project.pbxproj');

        var pbxProject;
        if (context.opts.cordova.project) {
          pbxProject = context.opts.cordova.project.parseProjectFile(context.opts.projectRoot).xcode;
        } else {
          pbxProject = xcode.project(projectPath);
          pbxProject.parseSync();
        }

        pbxProject.addResourceFile( "Ringtone.caf");

        // write the updated project file
        fs.writeFileSync(projectPath, pbxProject.writeSync());
        console.log("Added Ringtone.caf to project '" + projName + "'");

        deferral.resolve();
      });
    }
  });

  return deferral.promise;
};
