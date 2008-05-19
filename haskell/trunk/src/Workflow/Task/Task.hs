
module Workflow.Task.Task where

import Workflow.EngineTypes
import Workflow.Engine
import qualified Data.Map as Map
import Data.Dynamic

data TaskDef =
    TaskDef {
       taskDefName :: String,
       taskDefDesc :: String
   }
   deriving (Typeable)

data TaskState = Open | Complete | Rejected
 deriving (Show,Eq)

data Task =
  Task {
    getTokId       :: Int,
    taskId         :: String,
    taskName       :: String,
    taskDesc       :: String,
    taskState      :: TaskState,
    taskRejectable :: Bool
  }

showTaskList :: [Task] -> IO ()
showTaskList tasks =
  do putStrLn "Tasks:"
     if (null tasks)
       then putStrLn "  No tasks to display"
       else showTasks tasks 1

showTasks :: [Task] -> Int -> IO ()
showTasks [] _ = do return ()
showTasks (task:rest) counter =
 do putStrLn $ show counter ++ ": " ++ (taskName task) ++ " - " ++ show (taskState task)
    showTasks rest (counter + 1)

acceptAndCreateTask :: (WfEngine e) => e -> NodeToken -> WfProcess [Task] -> IO (WfProcess [Task])
acceptAndCreateTask engine token process =
    do process <- case (attrValue process token key) of
                      Just val -> setTokenAttr engine process token key $ show $ (1 + (read val)::Int)
                      Nothing  -> setTokenAttr engine process token key "1"
       return process { userData = task: (userData process) }
    where
        task = newTask process token
        key  = taskName task

newTask :: WfProcess [Task] -> NodeToken -> Task
newTask wf token = Task (tokenId token) (show theNodeId) taskName taskDesc Open hasReject
    where
        node      = nodeForToken token (wfGraph wf)
        theNodeId = nodeId node
        taskDef   = case (nodeExtra node) of
                         NodeExtra dyn -> fromDyn dyn (TaskDef "default" "default")
                         NoNodeExtra   -> TaskDef "default" "default"
        taskName  = taskDefName taskDef
        taskDesc  = taskDefDesc taskDef
        hasReject = not.null $ filter (\arc -> arcName arc =="reject") $ ((graphOutputArcs.wfGraph) wf) Map.! theNodeId

closeTask :: Task -> WfProcess [Task] -> TaskState -> WfProcess [Task]
closeTask task wf newState = wf { userData = newTaskList }
  where
    newTaskList = map (closeIfMatches) (userData wf)
    closeIfMatches t = if (taskId t == taskId task && (taskState t == Open))
                           then t { taskState=newState }
                           else t

completeTask :: (WfEngine e) => e -> Task -> WfProcess [Task] -> IO (WfProcess [Task])
completeTask engine task wf = completeDefaultExecution engine token (closeTask task wf Complete)
  where
    token = getNodeTokenForId (getTokId task) wf

rejectTask :: (WfEngine e) => e -> Task -> WfProcess [Task] -> IO (WfProcess [Task])
rejectTask engine task wf = completeExecution engine token "reject" (closeTask task wf Rejected)
  where
    token = getNodeTokenForId (getTokId task) wf