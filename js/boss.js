/*
 * boss.js
 * created on 12/01/2010
 * Copyright (C) 2010 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 * http://www.gnu.org/copyleft/lesser.html
 */
var gaggle = {};

gaggle.GooseProxy = function(bridgeBoss, wrappedGoose) {
    this.getName = function() { return wrappedGoose.getName(); };
    this.setName = function(newName) { wrappedGoose.setName(newName); };
    this.update = function(currentGooseIds) { wrappedGoose.update(currentGooseIds); };
    this.handleNameList = function(source, namelist) {
        wrappedGoose.handleNameList(source, namelist);
    };

    this.handleNetwork = function(source, network) {
        wrappedGoose.handleNetwork(source, network);
    };
};

// all geese have the method getName()/setName()
gaggle.Boss = function(bridgeBoss) {
    var gooseUIDs = [];
    var gooseMap = [];
    var log = []; // a log of messages

    /*
     * A simple uniquification function for goose names based on NameUniquifier in GuiBoss.
     * We assume that all unique names are in the form <baseName>-<counter>, so we can
     * use a simple regexp to find the maximum count for a base name.
     */
    function uniqueNameFor(baseName) {
        var maxCount = 0;
        var regexp = new RegExp(baseName + '-(\\d)+');

        for (var i = 0; i < gooseUIDs.length; i++) {
            if (gooseUIDs[i].match(regexp)) {
                var comps = gooseUIDs[i].split('-');
                var count = parseInt(comps[comps.length - 1]);
                if (count > maxCount) maxCount = count;
            }
        }
        var uniqueCount = maxCount + 1;
        return (uniqueCount < 10) ? baseName + '-0' + uniqueCount : baseName + '-' + uniqueCount;
    }
    /*
     * A simple logging function, for diagnostic purposes. Using this method assumes that
     * jQuery >= 1.4.3 is used within the page
     */
    this.log = function(source, msg) {
        var logMsg = '<span style="color: blue;">' + source +
            ':</span> \'' + msg + "\'";
        log.push(logMsg);
        var logText = '<br>';
        for (var i = 0; i < log.length; i++) {
            logText += log[i] + '<br>';
        }
        $('#gaggle-log-text').replaceWith('<div id="gaggle-log-text">' + logText + '</div>');
    };
    this.registerWithProxy = function (goose) {
        return this.register(new gaggle.GooseProxy(bridgeBoss, goose));
    };

    function updateGeese() {
        for (var i = 0; i < gooseUIDs.length; i++) {
            gooseMap[gooseUIDs[i]].update(gooseUIDs);
        }
    }

    // service interface
    this.register = function(goose) {
        var uniqueName = uniqueNameFor(goose.getName());
        goose.setName(uniqueName);
        gooseUIDs[gooseUIDs.length] = uniqueName;
        gooseMap[uniqueName] = goose;
        updateGeese();
        this.log('boss', 'registered goose with name: ' + goose.getName());
        return uniqueName;
    };
    this.unregister = function(gooseId) {
        gooseMap[gooseId] = undefined;
        var index = gooseUIDs.indexOf(gooseId);
        if (index >= 0) { gooseUIDs.splice(index, 1); }
        updateGeese();
        this.log('boss', 'unregistered goose with id: ' + gooseId);
    };
    this.broadcast = function(method, source, target, data) {
        this.log('boss', 'broadcast(), method = ' + method + ', source = ' + source + ' target: ' + target);
        if (target === 'Boss') {
            for (var i = 0; i < gooseUIDs.length; i++) {
                if (gooseUIDs[i] != source) {
                    gooseMap[gooseUIDs[i]][method](source, data);
                }
            }
        } else {
            gooseMap[target][method](source, data);
        }
    };
    this.broadcastNamelist = function(source, target, namelist) {
        this.broadcast('handleNameList', source, target, namelist);
    };
    this.broadcastNetwork = function(source, target, network) {
        this.broadcast('handleNetwork', source, target, network);
    };
    this.broadcastCluster = function(source, target, cluster) {
        this.broadcast('handleCluster', source, target, cluster);
    };
    this.broadcastTuple = function(source, target, tuple) {
        this.broadcast('handleTuple', source, target, tuple);
    };
    this.broadcastMatrix = function(source, target, matrix) {
        this.broadcast('handleMatrix', source, target, matrix);
    };
    this.exists = function(gooseId) {
        return gooseMap[gooseId] !== undefined;
    };
    this.numGeese = function() {
        return gooseUIDs.length;
    };
};

