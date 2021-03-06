{-
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
-}

--Author: Paul Lorenz

module Main where

import IO
import Data.Char
import Data.Map as Map hiding (filter, map, null)

import Database.HDBC
import Database.HDBC.PostgreSQL
import Database.HDBC.Types

import Workflow.Sarasvati.Engine
import Workflow.Sarasvati.Error
import Workflow.Sarasvati.DatabaseLoader
import Workflow.Sarasvati.DatabaseWfEngine

import Workflow.Example.Task
import Workflow.Example.ExampleCommon


main :: IO ()
main =
    do hSetBuffering stdout NoBuffering
       wfList <- getWorkflowList
       selectWorkflow wfList

openDbConnection :: IO Connection
openDbConnection = connectPostgreSQL "port=5432"

selectWorkflow :: [String] -> IO ()
selectWorkflow wfList =
    do putStrLn "\n-=Available workflows=-"
       showWorkflows wfList 1
       putStr "\nSelect workflow: "
       wf <- getLine
       if ((not $ null wf) && all (isDigit) wf)
         then handleAll (useWorkflow wfList (((read wf)::Int) - 1))
         else do putStrLn $ "ERROR: " ++ wf ++ " is not a valid workflow"
       selectWorkflow wfList
    where
        handleAll = (handleSql handleDbError).(handleWf handleLoadError)

useWorkflow :: [String] -> Int -> IO ()
useWorkflow wfList idx
    | length wfList <= idx = do putStrLn "ERROR: Invalid workflow number"
    | otherwise            = do conn <- openDbConnection
                                graph <- loadLatestGraph conn (wfList !! idx) typeMap
                                putStrLn "Running workflow"
                                putStrLn (show graph)
                                disconnect conn
                                runWorkflow graph
   where
       typeMap = Map.fromList [ ("task", loadTask) ]

runWorkflow :: WfGraph -> IO ()
runWorkflow graph =
    do conn <- openDbConnection
       let engine = DatabaseWfEngine conn
       result <- startWorkflow engine nodeTypeMap predMap Map.empty graph []
       case (result) of
           Left msg -> do rollback conn
                          putStrLn msg
           Right wf -> do commit conn
                          processTasks engine wf
       disconnect conn

nodeTypeMap :: Map.Map String (NodeType [Task])
nodeTypeMap = Map.fromList
                [ ( "node",  NodeType evalGuardLang completeDefaultExecution ),
                  ( "task",  NodeType evalGuardLang acceptAndCreateTask ),
                  ( "init",  NodeType evalGuardLang acceptInit ),
                  ( "dump",  NodeType evalGuardLang acceptDump ) ]

predMap :: Map.Map String (NodeToken -> WfProcess a -> IO Bool)
predMap = Map.fromList [ ("isRandOdd", predIsRandOdd),
                         ("isRandEven", predIsRandEven),
                         ("isTenthIteration", predIsTenthIteration) ]

getWorkflowList :: IO [String]
getWorkflowList =
    do conn <- openDbConnection
       result <- withTransaction conn (getWorkflowListFromDb)
       disconnect conn
       return result

getWorkflowListFromDb :: (IConnection conn) => conn -> IO [String]
getWorkflowListFromDb conn =
    do rows <- quickQuery' conn sql []
       return $ map (fromSql.head) rows
    where
        sql = "select distinct name from wf_graph order by name asc"

handleLoadError :: WfError -> IO ()
handleLoadError (WfError msg) =
    do putStrLn msg
       return $ ()

handleWorkflowError :: WfException -> IO ()
handleWorkflowError (WfException msg) =
    do putStrLn msg
       return $ ()

handleDbError :: SqlError -> IO ()
handleDbError sqlError =
    do putStrLn msg
       return ()
    where
       msg = "Database error: " ++ (seErrorMsg sqlError)