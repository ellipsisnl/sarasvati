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

package com.googlecode.sarasvati.rubric.lang;

import java.util.Date;

import com.googlecode.sarasvati.rubric.env.RubricEnv;
import com.googlecode.sarasvati.rubric.visitor.RubricVisitor;

public class RubricStmtDateSymbol extends AbstractRubricStmt
{
  protected String symbol;

  public RubricStmtDateSymbol (String symbol)
  {
    this.symbol = symbol;
  }

  public String getSymbol ()
  {
    return symbol;
  }

  public void setSymbol (String symbol)
  {
    this.symbol = symbol;
  }

  @Override
  public Date eval (RubricEnv env)
  {
    return env.evalDateFunction( symbol );
  }

  @Override
  public void traverse (RubricVisitor visitor)
  {
    visitor.visit( this );
  }

  @Override
  public boolean isEqualTo (RubricStmt stmt)
  {
    return stmt.isDateSymbol() && stmt.asDateSymbol().getSymbol().equals( symbol );
  }

  @Override
  public RubricStmtDateSymbol asDateSymbol ()
  {
    return this;
  }

  @Override
  public boolean isDateSymbol ()
  {
    return true;
  }
}
