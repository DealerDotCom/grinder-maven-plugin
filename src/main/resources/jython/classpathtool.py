# Copyright (C) 2008-2011, Travis Bear
# All rights reserved.
#
# This file is part of Grinder Analyzer.
#
# Grinder Analyzer is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Grinder Analyzer is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Grinder Analyzer; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#
#   DESCRIPTION:  Helper script used by the diff tool and grinder analyzer to
#                 set the java classpath and validate run location.
#        AUTHOR:  Travis Bear
#       CREATED:  10/23/2008 17:31:24

import os
import sys

from com.fides import MavenUtilities


# Ensure we're on jython and not cpython
if os.name != 'java':
    print "FATAL: cpython is not supported. This program must be invoked with jython 2.2.1 or later."
    sys.exit(1)


# Try to find the grinder analyzer dir.
GA_NAME="jython"

fullCurrentDir = MavenUtilities.getCurrentDir()+os.sep+GA_NAME

#set from java
cdir=os.path.split(fullCurrentDir)[1]

if cdir.find(GA_NAME) != 0:
#    if not fullCurrentDir.endswith("grinderAnalyzer/scripts"):
        print "FATAL: This program can only be run from within the %s directory." %GA_NAME
        print "Current dir: %s, cdir: %s" %(os.getcwd(),cdir)
        sys.exit(1)

# Set the classpath
libDir=fullCurrentDir+os.sep+"lib"

# Import modules from plugin jar file
commons_collection_jar = MavenUtilities.getPluginAbsolutePath(
                                                              'commons-collections',
                                                              'commons-collections',
                                                              '3.2',
                                                              '');
sys.path.insert(0, commons_collection_jar)

commons_lang_jar = MavenUtilities.getPluginAbsolutePath(
                                                        'commons-lang',
                                                        'commons-lang',
                                                        '2.3',
                                                        '');
sys.path.insert(0, commons_lang_jar)

jfreechart_jar = MavenUtilities.getPluginAbsolutePath(
                                              'jfree',
                                              'jfreechart',
                                              '1.0.13',
                                              '');
sys.path.insert(0, jfreechart_jar)

velocity_jar = MavenUtilities.getPluginAbsolutePath(
                                            'velocity',
                                            'velocity',
                                            '1.5',
                                            '');
sys.path.insert(0, velocity_jar)

log4j_jar = MavenUtilities.getPluginAbsolutePath(
                                       'log4j',
                                       'log4j',
                                       '1.2.16',
                                       '');
sys.path.insert(0, log4j_jar)

htmllexer_jar = MavenUtilities.getPluginAbsolutePath(
                                            'org.htmlparser',
                                            'htmllexer',
                                            '2.1',
                                            '');
sys.path.insert(0, htmllexer_jar)

htmlparser_jar = MavenUtilities.getPluginAbsolutePath(
                                              'nu.validator.htmlparser',
                                              'htmlparser',
                                              '1.2.1',
                                              '');
sys.path.insert(0, htmlparser_jar)

jcommon_jar = MavenUtilities.getPluginAbsolutePath(
                                           'jfree',
                                           'jcommon',
                                           '1.0.12',
                                           '');
sys.path.insert(0, jcommon_jar)

jars=os.listdir(libDir)
for jar in jars:
   sys.path.insert(0,libDir + os.sep + jar)



