/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.gedcom4j.parser;

import org.gedcom4j.model.ChangeDate;
import org.gedcom4j.model.NoteRecord;
import org.gedcom4j.model.StringTree;
import org.gedcom4j.model.UserReference;

/**
 * Parser for a {@link NoteRecord} object
 * 
 * @author frizbog
 */
class NoteRecordParser extends AbstractParser<NoteRecord> {

    /**
     * Constructor
     * 
     * @param gedcomParser
     *            a reference to the root {@link GedcomParser}
     * @param stringTree
     *            {@link StringTree} to be parsed
     * @param loadInto
     *            the object we are loading data into
     */
    NoteRecordParser(GedcomParser gedcomParser, StringTree stringTree, NoteRecord loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void parse() {
        if (stringTree.getXref() != null && !stringTree.getXref().isEmpty() && referencesAnotherNode(stringTree)) {
            addWarning("NOTE line has both an XREF_ID (" + stringTree.getXref() + ") and SUBMITTER_TEXT (" + stringTree.getValue()
                    + ") value between @ signs - " + "treating SUBMITTER_TEXT as string, not a cross-reference");
        }
        loadInto.getLines(true).add(stringTree.getValue());
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.CONCATENATION.equalsText(ch.getTag())) {
                    if (loadInto.getLines().isEmpty()) {
                        loadInto.getLines(true).add(ch.getValue());
                    } else {
                        String lastNote = loadInto.getLines().get(loadInto.getLines().size() - 1);
                        if (lastNote == null || lastNote.length() == 0) {
                            loadInto.getLines().set(loadInto.getLines().size() - 1, ch.getValue());
                        } else {
                            loadInto.getLines().set(loadInto.getLines().size() - 1, lastNote + ch.getValue());
                        }
                    }
                } else if (Tag.CONTINUATION.equalsText(ch.getTag())) {
                    loadInto.getLines(true).add(ch.getValue() == null ? "" : ch.getValue());
                } else if (Tag.SOURCE.equalsText(ch.getTag())) {
                    new CitationListParser(gedcomParser, ch, loadInto.getCitations(true)).parse();
                } else if (Tag.REFERENCE.equalsText(ch.getTag())) {
                    UserReference u = new UserReference();
                    loadInto.getUserReferences(true).add(u);
                    new UserReferenceParser(gedcomParser, ch, u).parse();
                } else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    loadInto.setRecIdNumber(parseStringWithCustomFacts(ch));
                } else if (Tag.CHANGED_DATETIME.equalsText(ch.getTag())) {
                    ChangeDate changeDate = new ChangeDate();
                    loadInto.setChangeDate(changeDate);
                    new ChangeDateParser(gedcomParser, ch, changeDate).parse();
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }
    }

}
