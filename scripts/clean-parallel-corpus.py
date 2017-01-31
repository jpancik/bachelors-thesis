# Written for Python 2.7.10
# Juraj Pancik 2017

import sys
import re

if len(sys.argv) < 3:
    print >> sys.stderr, "Please specify corpuses filenames as first (source) and second (target) argument. Use third argument to limit count. Pipe standard output into target file."
else:
    filenameSource = sys.argv[1]
    filenameTarget = sys.argv[2]
    print >> sys.stderr, "Starting to clean parallel corpuses: ", filenameSource, filenameTarget, "."

    fileSource = open(filenameSource, "r").readlines()
    fileTarget = open(filenameTarget, "r").readlines()

    if len(fileSource) != len(fileTarget):
        print >> sys.stderr, "Number of lines doesn't match. Exiting.", len(fileSource), len(fileTarget)
        exit()

    counter = 0
    valid = 0

    regex = re.compile(r'^.*[0-9-=*"/\\()\'].*$')
    minLength = 60
    maxLength = 100

    workloadLength = 1234567890
    if len(sys.argv) > 3:
        workloadLength = int(sys.argv[3])

    print "["
    for i in xrange(len(fileSource)):
        lineSource = fileSource[i]
        lineTarget = fileTarget[i]

        counter += 1
        if counter % 1000 == 0:
            print >> sys.stderr, "Finished loading", counter, "lines."
        foundSource = regex.search(lineSource)
        foundTarget = regex.search(lineTarget)
        # Remove sentences with numbers and random characters. Also remove short lines.
        if foundSource is None and foundTarget is None and len(lineSource) > minLength and len(lineSource) < maxLength and len(lineTarget) > minLength and len(lineTarget) < maxLength and lineSource.replace(" ", "") != lineTarget.replace(" ", ""):
            valid += 1
            print "\t{"
            print "\t\t\"original\":\"" + lineSource[:-1] + "\","
            print "\t\t\"translated\":\"" + lineTarget[:-1] + "\""
            print "\t},"

            if valid >= workloadLength:
                break
    print "]"

    print >> sys.stderr, "Done and produced", valid , "lines. Don't forget to remove last comma from JSON list."