#!/bin/gawk
# $Id: //open/util/bin/checkFile#13 $
# Checks that a file is valid.

function error(fname, linum, msg) {
    printf "%s: %d: %s\n", fname, linum, msg;
    if (0) print; # for debug
}
function matchFile(fname) {
    return fname ~ "/mondrian/" \
       || fname ~ "/org/olap4j/" \
       || fname ~ "/aspen/" \
       || fname ~ "/farrago/" \
       || fname ~ "/fennel/" \
       || fname ~ "/com/sqlstream/" \
       || !lenient;
}
function isCpp(fname) {
    return fname ~ /\.(cpp|h)$/;
}
function isJava(fname) {
    return !isCpp(fname);
}
function push(val) {
   switchStack[switchStackLen++] = val;
}
function pop() {
   --switchStackLen
   val = switchStack[switchStackLen];
   delete switchStack[switchStackLen];
   return val;
}
BEGIN {
    # pre-compute regexp for single-quoted strings
    apos = sprintf("%c", 39);
    lf = sprintf("%c", 13);
    pattern = apos "(\\" apos "|[^" apos "])" apos;
    if (0) printf "maxLineLength=%s lenient=%s\n", maxLineLength, lenient;
}
{
    if (previousLineEndedInCloseBrace > 0) {
        --previousLineEndedInCloseBrace;
    }
    if (previousLineEndedInOpenBrace > 0) {
        --previousLineEndedInOpenBrace;
    }
    if (previousLineWasEmpty > 0) {
        --previousLineWasEmpty;
    }
    s = $0;
    # remove DOS linefeeds
    gsub(lf, "", s);
    # replace strings
    gsub(/"(\\"|[^"\\]|\\[^"])*"/, "string", s);
    # replace single-quoted strings
    gsub(pattern, "string", s);
    # replace {: and :} in .cup files
    if (fname ~ /\.cup$/) {
        gsub(/{:/, "{", s);
        gsub(/:}/, "}", s);
        gsub(/:/, " : ", s);
    }
    if (inComment && $0 ~ /\*\//) {
        # end of multiline comment "*/"
        inComment = 0;
        gsub(/^.*\*\//, "/* comment */", s);
    } else if (inComment) {
        s = "/* comment */";
    } else if ($0 ~ /\/\*/ && $0 !~ /\/\*.*\*\//) {
        # beginning of multiline comment "/*"
        inComment = 1;
        gsub(/\/\*.*$/, "/* comment */", s);
    } else {
        # mask out /* */ comments
        gsub(/\/\*.*\*\//, "/* comment */", s);
    }
    # mask out // comments
    gsub(/\/\/.*$/, "// comment", s);
}
/ $/ {
    error(fname, FNR, "Line ends in space");
}
/[\t]/ {
    if (matchFile(fname)) {
        error(fname, FNR, "Tab character");
    }
}
/^$/ {
    if (matchFile(fname) && previousLineEndedInOpenBrace) {
        error(fname, FNR, "Empty line following open brace");
    }
}
/^ +}( catch| finally| while|[;,)])/ ||
/^ +}$/ {
    if (matchFile(fname) && previousLineWasEmpty) {
        error(fname, FNR - 1, "Empty line before close brace");
    }
}
s ~ /\<if\>.*;$/ {
    if (!matchFile(fname)) {}
    else {
        error(fname, FNR, "if followed by statement on same line");
    }
}
s ~ /\<(if) *\(/ {
    if (!matchFile(fname)) {
    } else if (s !~ /\<(if) /) {
        error(fname, FNR, "if must be followed by space");
    } else if (s ~ / else if /) {
    } else if (s ~ /^#if /) {
    } else if (s !~ /^(    )*(if)/) {
        error(fname, FNR, "if must be correctly indented");
    }
}
s ~ /\<(while) *\(/ {
    if (!matchFile(fname)) {
    } else if (s !~ /\<(while) /) {
        error(fname, FNR, "while must be followed by space");
    } else if (s ~ /} while /) {
    } else if (s !~ /^(    )+(while)/) {
        error(fname, FNR, "while must be correctly indented");
    }
}
s ~ /\<(for|switch|synchronized|} catch) *\(/ {
    if (!matchFile(fname)) {}
    else if (s !~ /^(    )*(for|switch|synchronized|} catch)/) {
        error(fname, FNR, "for/switch/synchronized/catch must be correctly indented");
    } else if (s !~ /\<(for|switch|synchronized|} catch) /) {
        error(fname, FNR, "for/switch/synchronized/catch must be followed by space");
    }
}
s ~ /\<(if|while|for|switch)\>/ {
    # Check single-line if statements, such as
    #   if (condition) return;
    # We recognize such statements because there are equal numbers of open and
    # close parentheses.
    opens = s;
    gsub(/[^(]/, "", opens);
    closes = s;
    gsub(/[^)]/, "", closes);
    if (!matchFile(fname)) {
    } else if (s ~ /{( *\/\/ comment)?$/) {
        # lines which end with { and optional comment are ok
    } else if (s ~ /{.*\\$/ && isCpp(fname)) {
        # lines which end with backslash are ok in c++ macros
    } else if (s ~ /} while/) {
        # lines like "} while (foo);" are ok
    } else if (s ~ /^#/) {
        # lines like "#if 0" are ok
    } else if (s ~ /if \(true|false\)/) {
        # allow "if (true)" and "if (false)" because they are
        # used for commenting
    } else if (length(opens) == length(closes)  \
               && length($0) != 79              \
               && length($0) != 80)
    {
        error(fname, FNR, "single-line if/while/for/switch must end in {");
    }
}
s ~ /[[:alnum:]]\(/ &&
s !~ /\<(if|while|for|switch|assert)\>/ {
    ss = s;
    gsub(/.*[[:alnum:]]\(/, "(", ss);
    opens = ss;
    gsub(/[^(]/, "", opens);
    closes = ss;
    gsub(/[^)]/, "", closes);
    if (length(opens) > length(closes)) {
        if (s ~ /,$/) {
            bras = s;
            gsub(/[^<]/, "", bras);
            kets = s;
            gsub(/[^>]/, "", kets);
            if (length(bras) > length(kets)) {
                # Ignore case like 'for (Map.Entry<Foo,{nl} Bar> entry : ...'
            } else if (s ~ / for /) {
                # Ignore case like 'for (int i = 1,{nl} j = 2; i < j; ...'
            } else {
                error(                                                  \
                    fname, FNR,                                         \
                    "multi-line parameter list should start with newline");
            }
        } else if (s ~ /[;(]( *\\)?$/) {
            # If open paren is at end of line (with optional backslash
            # for macros), we're fine.
        } else if (s ~ /@.*\({/) {
            # Ignore Java annotations.
        } else {
            error(                                                      \
                fname, FNR,                                             \
                "Open parenthesis should be at end of line (function call spans several lines)");
        }
    }
}
s ~ /\<switch\>/ {
    push(switchCol);
    switchCol = index($0, "switch");
}
s ~ /{/ {
    braceCol = index($0, "{");
    if (braceCol == switchCol) {
        push(switchCol);
    }
}
s ~ /}/ {
    braceCol = index($0, "}");
    if (braceCol == switchCol) {
        switchCol = pop();
    }
}
s ~ /\<(case|default)\>/ {
    caseDefaultCol = match($0, /case|default/);
    if (!matchFile(fname)) {}
    else if (caseDefaultCol != switchCol) {
        error(fname, FNR, "case/default must be aligned with switch");
    }
}
s ~ /\<assert\>/ {
    if (!matchFile(fname)) {}
    else if (isCpp(fname)) {} # rule only applies to java
    else if (s !~ /^(    )+(assert)/) {
        error(fname, FNR, "assert must be correctly indented");
    } else if (s !~ /\<assert /) {
        error(fname, FNR, "assert must be followed by space");
    }
}
s ~ /\<return\>/ {
    if (!matchFile(fname)) {}
    else if (isCpp(fname) && s ~ /^#/) {
        # ignore macros
    } else if (s !~ /^(    )+(return)/) {
        error(fname, FNR, "return must be correctly indented");
    } else if (s !~ /\<return[ ;]/ && s !~ /\<return$/) {
        error(fname, FNR, "return must be followed by space or ;");
    }
}
s ~ /\<throw\>/ {
    if (!matchFile(fname)) {}
    else if (isCpp(fname)) {
        # cannot yet handle C++ cases like 'void foo() throw(int)'
    } else if (s !~ /^(    )+(throw)/) {
        error(fname, FNR, "throw must be correctly indented");
    } else if (s !~ /\<throw / && s !~ /\<throw$/) {
        error(fname, FNR, "throw must be followed by space");
    }
}
s ~ /\<else\>/ {
    if (!matchFile(fname)) {}
    else if (isCpp(fname) && s ~ /^# *else$/) {} # ignore "#else"
    else if (s !~ /^(    )+} else (if |{$|{ *\/\/|{ *\/\*)/) {
        error(fname, FNR, "else must be preceded by } and followed by { or if and correctly indented");
    }
}
s ~ /\<do\>/ {
    if (!matchFile(fname)) {}
    else if (s !~ /^(    )*do {/) {
        error(fname, FNR, "do must be followed by space {, and correctly indented");
    }
}
s ~ /\<try\>/ {
    if (!matchFile(fname)) {}
    else if (s !~ /^(    )+try {/) {
        error(fname, FNR, "try must be followed by space {, and correctly indented");
    }
}
s ~ /\<catch\>/ {
    if (!matchFile(fname)) {}
    else if (s !~ /^(    )+} catch /) {
        error(fname, FNR, "catch must be preceded by }, followed by space, and correctly indented");
    }
}
s ~ /\<finally\>/ {
    if (!matchFile(fname)) {}
    else if (s !~ /^(    )+} finally {/) {
        error(fname, FNR, "finally must be preceded by }, followed by space {, and correctly indented");
    }
}
match(s, /([]A-Za-z0-9()])(+|-|\*|\^|\/|%|=|==|+=|-=|\*=|\/=|>=|<=|!=|&|&&|\||\|\||^|\?|:) *[A-Za-z0-9(]/, a) {
    # < and > are not handled here - they have special treatment below
    if (!matchFile(fname)) {}
#    else if (s ~ /<.*>/) {} # ignore templates
    else if (a[2] == "-" && s ~ /\(-/) {} # ignore case "foo(-1)"
    else if (a[2] == "-" && s ~ /[eE][+-][0-9]/) {} # ignore e.g. 1e-5
    else if (a[2] == "+" && s ~ /[eE][+-][0-9]/) {} # ignore e.g. 1e+5
    else if (a[2] == ":" && s ~ /(case.*|default):$/) {} # ignore e.g. "case 5:"
    else if (isCpp(fname) && s ~ /[^ ][*&]/) {} # ignore e.g. "Foo* p;" in c++ - debatable
    else if (isCpp(fname) && s ~ /\<operator.*\(/) {} # ignore e.g. "operator++()" in c++
    else if (isCpp(fname) && a[2] == "/" && s ~ /#include/) {} # ignore e.g. "#include <x/y.hpp>" in c++
    else {
        error(fname, FNR, "operator '" a[2] "' must be preceded by space");
    }
}
match(s, /([]A-Za-z0-9() ] *)(+|-|\*|\^|\/|%|=|==|+=|-=|\*=|\/=|>=|<=|!=|&|&&|\||\|\||^|\?|:|,)[A-Za-z0-9(]/, a) {
    if (!matchFile(fname)) {}
#    else if (s ~ /<.*>/) {} # ignore templates
    else if (a[2] == "-" && s ~ /(\(|return |case |= )-/) {} # ignore prefix -
    else if (a[2] == ":" && s ~ /(case.*|default):$/) {} # ignore e.g. "case 5:"
    else if (s ~ /, *-/) {} # ignore case "foo(x, -1)"
    else if (s ~ /-[^ ]/ && s ~ /[^A-Za-z0-9] -/) {} # ignore case "x + -1" but not "x -1" or "3 -1"
    else if (a[2] == "-" && s ~ /[eE][+-][0-9]/) {} # ignore e.g. 1e-5
    else if (a[2] == "+" && s ~ /[eE][+-][0-9]/) {} # ignore e.g. 1e+5
    else if (a[2] == "*" && isCpp(fname) && s ~ /\*[^ ]/) {} # ignore e.g. "Foo *p;" in c++
    else if (a[2] == "&" && isCpp(fname) && s ~ /&[^ ]/) {} # ignore case "foo(&x)" in c++
    else if (isCpp(fname) && s ~ /\<operator[^ ]+\(/) {} # ignore e.g. "operator++()" in c++
    else if (isCpp(fname) && a[2] == "/" && s ~ /#include/) {} # ignore e.g. "#include <x/y.hpp>" in c++
    else if (lenient && fname ~ /(fennel)/ && a[1] = ",") {} # not enabled yet
    else {
        error(fname, FNR, "operator '" a[2] "' must be followed by space");
    }
}
match(s, / (+|-|\*|\/|==|>=|<=|!=|<<|<<<|>>|&|&&|\|\||\?|:)$/, a) || \
match(s, /(\.|->)$/, a) {
    if (lenient && fname ~ /(aspen)/ && a[1] != ":") {} # not enabled yet
    else if (lenient && fname ~ /(fennel|farrago|aspen)/ && a[1] = "+") {} # not enabled yet
    else if (a[1] == ":" && s ~ /(case.*|default):$/) {
        # ignore e.g. "case 5:"
    } else if ((a[1] == "*" || a[1] == "&") && isCpp(fname) && s ~ /^[[:alnum:]:_ ]* [*&]$/) {
        # ignore e.g. "const int *\nClass::Subclass2::method(int x)"
    } else {
        error(fname, FNR, "operator '" a[1] "' must not be at end of line");
    }
}
s ~ /\<[[:digit:][:lower:]][[:alnum:]_]*</ {
    # E.g. "p<" but not "Map<"
    if (!matchFile(fname)) {}
    else if (isCpp(fname)) {} # in C++ 'xyz<5>' could be a template
    else {
        error(fname, FNR, "operator '<' must be preceded by space");
    }
}
s ~ /\<[[:digit:][:lower:]][[:alnum:]_]*>/ {
    # E.g. "g>" but not "String>" as in "List<String>"
    if (!matchFile(fname)) {}
    else if (isCpp(fname)) {} # in C++ 'xyz<int>' could be a template
    else {
        error(fname, FNR, "operator '>' must be preceded by space");
    }
}
match(s, /<([[:digit:][:lower:]][[:alnum:].]*)\>/, a) {
    if (!matchFile(fname)) {}
    else if (isCpp(fname)) {
        # in C++, template and include generate too many false positives
    } else if (isJava(fname) && a[1] ~ /(int|char|long|boolean|byte|double|float)/) {
        # Allow e.g. 'List<int[]>'
    } else if (isJava(fname) && a[1] ~ /^[[:lower:]]+\./) {
        # Allow e.g. 'List<java.lang.String>'
    } else {
        error(fname, FNR, "operator '<' must be followed by space");
    }
}
match(s, /^(.*[^-])>([[:digit:][:lower:]][[:alnum:]]*)\>/, a) {
    if (!matchFile(fname)) {}
    else if (isJava(fname) && a[1] ~ /.*\.<.*/) {
        # Ignore 'Collections.<Type>member'
    } else {
        error(fname, FNR, "operator '>' must be followed by space");
    }
}
s ~ /[[(] / {
    if (!matchFile(fname)) {}
    else if (s ~ /[[(] +\\$/) {} # ignore '#define foo(   \'
    else {
        error(fname, FNR, "( or [ must not be followed by space");
    }
}
s ~ / [])]/ {
    if (!matchFile(fname)) {}
    else if (s ~ /^ *\)/ && previousLineEndedInCloseBrace) {} # ignore "bar(new Foo() { } );"
    else {
        error(fname, FNR, ") or ] must not be followed by space");
    }
}
s ~ /}/ {
    if (!matchFile(fname)) {}
    else if (s !~ /}( |;|,|$|\))/) {
        error(fname, FNR, "} must be followed by space");
    } else if (s !~ /(    )*}/) {
        error(fname, FNR, "} must be at start of line and correctly indented");
    }
}
s ~ /{/ {
    if (!matchFile(fname)) {}
    else if (s ~ /(\]\)?|=) *{/) {} # ignore e.g. "(int[]) {1, 2}" or "int[] x = {1, 2}"
    else if (s ~ /\({/) {} # ignore e.g. @SuppressWarnings({"unchecked"})
    else if (s ~ /{ *(\/\/|\/\*)/) {} # ignore e.g. "do { // a comment"
    else if (s ~ / {}$/) {} # ignore e.g. "Constructor() {}"
    else if (s ~ / },$/) {} # ignore e.g. "{ yada },"
    else if (s ~ / };$/) {} # ignore e.g. "{ yada };"
    else if (s ~ / {};$/) {} # ignore e.g. "template <> class Foo<int> {};"
    else if (s ~ / },? *\/\/.*$/) {} # ignore e.g. "{ yada }, // comment"
    else if (s ~ /\\$/) {} # ignore multiline macros
    else if (s ~ /{}/) { # e.g. "Constructor(){}"
        error(fname, FNR, "{} must be preceded by space and at end of line");
    } else if (isCpp(fname) && s ~ /{ *\\$/) {
        # ignore - "{" can be followed by "\" in c macro
    } else if (s !~ /{$/) {
        error(fname, FNR, "{ must be at end of line");
    } else if (s !~ /(^| ){/) {
        error(fname, FNR, "{ must be preceded by space or at start of line");
    } else {
        opens = s;
        gsub(/[^(]/, "", opens);
        closes = s;
        gsub(/[^)]/, "", closes);
        if (0 && lenient && fname ~ /aspen/) {} # not enabled
        else if (length(closes) > length(opens)) {
            error(fname, FNR, "Open brace should be on new line (function call/decl spans several lines)");
        }
    }
}
s ~ /(^| )(class|interface|enum) / ||
s ~ /(^| )namespace / && isCpp(fname) {
    if (isCpp(fname) && s ~ /;$/) {} # ignore type declaration
    else {
        classDeclStartLine = FNR;
        t = s;
        gsub(/.*(class|interface|enum|namespace) /, "", t);
        gsub(/ .*$/, "", t);
        if (s ~ /template/) {
            # ignore case "template <class INSTCLASS> static void foo()"
            classDeclStartLine = 0;
        } else if (t ~ /[[:upper:]][[:upper:]][[:upper:]][[:upper:]]/     \
            && t !~ /LRU/ \
            && t !~ /WAL/ \
            && t !~ /classUUID/ \
            && t !~ /classSQLException/ \
            && t !~ /BBRC/ \
            && t !~ /_/ \
            && t !~ /EncodedSqlInterval/)
        {
            error(fname, FNR, "Class name " t " has consecutive uppercase letters");
        }
    }
}
s ~ / throws\>/ {
    if (s ~ /\(/) {
        funDeclStartLine = FNR;
    } else {
        funDeclStartLine = FNR - 1;
    }
}
length($0) > maxLineLength                      \
&& $0 !~ /@(throws|see|link)/                   \
&& $0 !~ /\$Id: /                               \
&& $0 !~ /^import /                             \
&& $0 !~ /http:/                                \
&& $0 !~ /\/\/ Expect "/                        \
&& s !~ /^ *(\+ |<< )?string\)?[;,]?$/ {
    error( \
        fname, \
        FNR, \
        "Line length (" length($0) ") exceeds " maxLineLength " chars");
}
/./ {
    lastNonEmptyLine = $0;
}
/}$/ {
    previousLineEndedInCloseBrace = 2;
}
/;$/ {
    funDeclStartLine = 0;
}
/{$/ {
    # Ignore open brace if it is part of class or interface declaration.
    if (classDeclStartLine) {
        if (classDeclStartLine < FNR \
            && $0 !~ /^ *{$/)
        {
            error(fname, FNR, "Open brace should be on new line (class decl spans several lines)");
        }
        classDeclStartLine = 0;
    } else {
        previousLineEndedInOpenBrace = 2;
    }
    if (funDeclStartLine) {
        if (funDeclStartLine < FNR \
            && $0 !~ /^ *{$/)
        {
            if (lenient && fname ~ /aspen/) {} # not enabled
            else error(fname, FNR, "Open brace should be on new line (function decl spans several lines)");
        }
        funDeclStartLine = 0;
    }
}
/^$/ {
    previousLineWasEmpty = 2;
}
{
    next;
}
END {
    # Compute basename. If fname="/foo/bar/baz.txt" then basename="baz.txt".
    basename = fname;
    gsub(".*/", "", basename);
    gsub(lf, "", lastNonEmptyLine);
    terminator = "// End " basename;
    if (matchFile(fname) && (lastNonEmptyLine != terminator)) {
        error(fname, FNR, sprintf("Last line should be %c%s%c", 39, terminator, 39));
    }
}

# End checkFile.awk
