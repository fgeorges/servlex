/****************************************************************************/
/*  File:       XProcProcessor.java                                         */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-04-15                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.processors;

import org.expath.servlex.components.Component;

/**
 * Abstract an XProc processor.
 *
 * @author Florent Georges
 */
public interface XProcProcessor
{
    public Component makePipeline(String uri);
    public Component makeStep(String uri, String ns, String local);

    /** The name of the input port. */
    public static final String INPUT_PORT_NAME  = "source";
    /** The name of the error port, for error handlers. */
    public static final String ERROR_PORT_NAME  = "user-data";
    /** The name of the output port. */
    public static final String OUTPUT_PORT_NAME = "result";

}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
