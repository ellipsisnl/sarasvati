/*
    This file is part of Sarasvati.

    Sarasvati is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Sarasvati is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Sarasvati.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2008 Paul Lorenz
*/

package com.googlecode.sarasvati.rubric.visitor;

import java.util.Date;

import com.googlecode.sarasvati.rubric.lang.RubricStmt;
import com.googlecode.sarasvati.rubric.lang.RubricStmtDateSymbol;
import com.googlecode.sarasvati.rubric.lang.RubricStmtRelativeDate;
import com.googlecode.sarasvati.rubric.lang.RubricStmtResult;

public class ResultTypeValidator extends RubricVisitorAdaptor
{
  public static boolean isResultOfType (RubricStmt stmt, Class<?> type)
  {
    ResultTypeValidator validator = new ResultTypeValidator( type );
    stmt.traverse( validator );
    return validator.isAllResultsMatchType();
  }

  protected Class<?> type;
  protected boolean allMatch = true;

  public ResultTypeValidator (Class<?> type)
  {
    this.type = type;
  }

  public boolean isAllResultsMatchType ()
  {
    return allMatch;
  }

  @Override
  public void visit (RubricStmtResult resultStmt)
  {
    if ( !type.isAssignableFrom( resultStmt.getResult().getClass() ) )
    {
      allMatch = false;
    }
  }

  @Override
  public void visit (RubricStmtDateSymbol dateSymbolStmt)
  {
    if ( !type.isAssignableFrom( Date.class ) )
    {
      allMatch = false;
    }
  }

  @Override
  public void visit (RubricStmtRelativeDate relativeDateStmt)
  {
    if ( !type.isAssignableFrom( Date.class ) )
    {
      allMatch = false;
    }
  }
}
