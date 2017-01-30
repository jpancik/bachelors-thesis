# Written for Python 2.7.10
# Juraj Pancik 2017

import sys
import re

DELIMITER=","

if len(sys.argv) == 1:
    print >> sys.stderr, "Please specify phrase-table file name as first argument. Pipe standard output into target file."
else:
    filename = sys.argv[1]
    print >> sys.stderr, "Starting to parse phrase-table into CSV file form file " + filename + "."

    counter = 0
    regex = re.compile(r'^([^\t|]+) \|\|\| ([^\t|]+) \|\|\| [^ ]+ [^ ]+ ([^ ]+) ([^ ]+) .*$')
    with open(filename, "r") as file:
        for line in file:
            counter += 1
            if counter % 1000000 == 0:
                print >> sys.stderr, "Finished loading", counter/1000, "k lines."
            found = regex.search(line)
            if found is not None:
                original = found.group(1)
                translated = found.group(2)
                directProbability = found.group(3)
                lexicalWeight = found.group(4)

                original.replace('"', '""')
                translated.replace('"', '""')

                print "\"" + original + "\"" + DELIMITER + "\"" + translated + "\"" + DELIMITER + directProbability + DELIMITER + lexicalWeight
                # print original, translated

    print >> sys.stderr, "Done."
