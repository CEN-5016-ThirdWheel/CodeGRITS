package trackers;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import utils.RelativePathGetter;
import utils.XMLWriter;

import javax.xml.parsers.DocumentBuilderFactory;


public final class IDETracker implements Disposable {
    boolean isTracking = false;
    Document iDETracking = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = iDETracking.createElement("ide_tracking");
    Element environment = iDETracking.createElement("environment");
    Element actions = iDETracking.createElement("actions");
    Element logs = iDETracking.createElement("logs");
    Element typings = iDETracking.createElement("typings");
    Element files = iDETracking.createElement("files");
    Element mouses = iDETracking.createElement("mouses");
    Element carets = iDETracking.createElement("carets");
    Element selections = iDETracking.createElement("selections");
    Element visibleAreas = iDETracking.createElement("visible_areas");
    String projectPath = "";
    String dataOutputPath = "";

    DocumentListener documentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            if (!isTracking) return;
            if (event.getDocument().getText().length() == 0) return;
            if (EditorFactory.getInstance().getEditors(event.getDocument()).length == 0) return;
            Editor currentEditor = EditorFactory.getInstance().getEditors(event.getDocument())[0];
            if (currentEditor != null && currentEditor.getEditorKind() == EditorKind.CONSOLE) {
                logFile("unknown", String.valueOf(System.currentTimeMillis()),
                        "contentChanged | CONSOLE", event.getDocument().getText());
                return;
            }
            VirtualFile changedFile = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (changedFile != null) {
                changedFilepath = changedFile.getPath();
                changedFileText = event.getDocument().getText();
            }
        }
    };

    EditorMouseListener editorMouseListener = new EditorMouseListener() {
        @Override
        public void mousePressed(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mousePressed");
            mouses.appendChild(mouseElement);
        }

        @Override
        public void mouseClicked(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseClicked");
            mouses.appendChild(mouseElement);
        }

        @Override
        public void mouseReleased(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseReleased");
            mouses.appendChild(mouseElement);
        }
    };

    EditorMouseMotionListener editorMouseMotionListener = new EditorMouseMotionListener() {
        @Override
        public void mouseMoved(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseMoved");
            mouses.appendChild(mouseElement);
        }

        @Override
        public void mouseDragged(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseDragged");
            mouses.appendChild(mouseElement);
        }
    };

    CaretListener caretListener = new CaretListener() {
        @Override
        public void caretPositionChanged(@NotNull CaretEvent e) {
            if (!isTracking) return;
            Element caretElement = iDETracking.createElement("caret");
            carets.appendChild(caretElement);
            caretElement.setAttribute("id", "caretPositionChanged");
            caretElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            caretElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            caretElement.setAttribute("line", String.valueOf(e.getNewPosition().line));
            caretElement.setAttribute("column", String.valueOf(e.getNewPosition().column));
        }
    };

    SelectionListener selectionListener = new SelectionListener() {
        @Override
        public void selectionChanged(@NotNull SelectionEvent e) {
            if (!isTracking) return;
            Element selectionElement = iDETracking.createElement("selection");
            selections.appendChild(selectionElement);
            selectionElement.setAttribute("id", "selectionChanged");
            selectionElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            selectionElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            LogicalPosition startLogicalPos = e.getEditor().offsetToLogicalPosition(e.getNewRange().getStartOffset());
            LogicalPosition endLogicalPos = e.getEditor().offsetToLogicalPosition(e.getNewRange().getEndOffset());
            selectionElement.setAttribute("start_position", startLogicalPos.line + ":" +
                    startLogicalPos.column);
            selectionElement.setAttribute("end_position", endLogicalPos.line + ":" +
                    endLogicalPos.column);
            selectionElement.setAttribute("selected_text", e.getEditor().getSelectionModel().getSelectedText());
        }
    };

    VisibleAreaListener visibleAreaListener = e -> {
        if (!isTracking) return;
        if (e.getEditor().getEditorKind() == EditorKind.MAIN_EDITOR) {
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            Element visibleAreaElement = iDETracking.createElement("visible_area");
            visibleAreas.appendChild(visibleAreaElement);
            visibleAreaElement.setAttribute("id", "visibleAreaChanged");
            visibleAreaElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            visibleAreaElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            visibleAreaElement.setAttribute("x", String.valueOf(e.getEditor().getScrollingModel().getHorizontalScrollOffset()));
            visibleAreaElement.setAttribute("y", String.valueOf(e.getEditor().getScrollingModel().getVerticalScrollOffset()));
            visibleAreaElement.setAttribute("width", String.valueOf(e.getEditor().getScrollingModel().getVisibleArea().width));
            visibleAreaElement.setAttribute("height", String.valueOf(e.getEditor().getScrollingModel().getVisibleArea().height));
        }
    };

    EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    Timer timer = new Timer();
    String changedFilepath = "";
    String changedFileText = "";
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (changedFilepath.length() > 0) {
                if (!isTracking) return;
                logFile(changedFilepath, String.valueOf(System.currentTimeMillis()),
                        "contentChanged | MAIN_EDITOR", changedFileText);
                changedFilepath = "";
            }
        }
    };

    IDETracker() throws ParserConfigurationException {
        iDETracking.appendChild(root);
        root.appendChild(environment);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        environment.setAttribute("screen_width", String.valueOf(size.getWidth()));
        environment.setAttribute("screen_height", String.valueOf(size.getHeight()));
        environment.setAttribute("ide_version", ApplicationInfo.getInstance().getFullVersion());
        environment.setAttribute("ide_name", ApplicationInfo.getInstance().getVersionName());

        root.appendChild(logs);
        root.appendChild(actions);
        root.appendChild(typings);
        root.appendChild(files);
        root.appendChild(mouses);
        root.appendChild(carets);
        root.appendChild(selections);
        root.appendChild(visibleAreas);

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                AnActionListener.TOPIC, new AnActionListener() {

                    @Override
                    public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
                        if (isTracking) {
                            Element actionElement = iDETracking.createElement("actions");
                            actionElement.setAttribute("id", ActionManager.getInstance().getId(action));
                            actionElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
                            actionElement.setAttribute("path", virtualFile != null ?
                                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
                            actions.appendChild(actionElement);
                        }
                    }

                    @Override
                    public void beforeEditorTyping(char c, @NotNull DataContext dataContext) {
                        if (isTracking) {
                            Element typingElement = iDETracking.createElement("typing");
                            typings.appendChild(typingElement);
                            typingElement.setAttribute("character", String.valueOf(c));
                            typingElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            VirtualFile virtualFile = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE);
                            typingElement.setAttribute("path", virtualFile != null ?
                                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);

                            Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
                            if (editor != null) {
                                Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
                                LogicalPosition logicalPos = primaryCaret.getLogicalPosition();
                                typingElement.setAttribute("line", String.valueOf(logicalPos.line));
                                typingElement.setAttribute("column", String.valueOf(logicalPos.column));
                            }
                        }
                    }
                });

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileOpened");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            logFile(file.getPath(), timestamp, "fileOpened", null);
                        }
                    }

                    @Override
                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileClosed");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            logFile(file.getPath(), timestamp, "fileClosed", null);
                        }
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);
                            fileElement.setAttribute("id", "selectionChanged");
                            fileElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            if (event.getOldFile() != null) {
                                fileElement.setAttribute("old_path",
                                        RelativePathGetter.getRelativePath(event.getOldFile().getPath(), projectPath));
                                logFile(event.getOldFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | OldFile", null);
                            }
                            if (event.getNewFile() != null) {
                                fileElement.setAttribute("new_path",
                                        RelativePathGetter.getRelativePath(event.getNewFile().getPath(), projectPath));
                                logFile(event.getNewFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | NewFile", null);
                            }
                        }
                    }
                });

        timer.schedule(timerTask, 0, 1);
    }

    public static IDETracker getInstance() throws ParserConfigurationException {
        return new IDETracker();
    }

    public void startTracking(Project project) {
        isTracking = true;
        environment.setAttribute("project_path", projectPath);
        environment.setAttribute("project_name", projectPath.substring(
                projectPath.lastIndexOf('/') + 1));
        editorEventMulticaster.addDocumentListener(documentListener, () -> {
        });
        editorEventMulticaster.addEditorMouseListener(editorMouseListener, () -> {
        });
        editorEventMulticaster.addEditorMouseMotionListener(editorMouseMotionListener, () -> {
        });
        editorEventMulticaster.addCaretListener(caretListener, () -> {
        });
        editorEventMulticaster.addSelectionListener(selectionListener, () -> {
        });
        editorEventMulticaster.addVisibleAreaListener(visibleAreaListener, () -> {
        });

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile file : fileEditorManager.getOpenFiles()) {
            logFile(file.getPath(), String.valueOf(System.currentTimeMillis()), "fileOpened", null);
        }
    }

    public void stopTracking() throws TransformerException {
        isTracking = false;
        editorEventMulticaster.removeDocumentListener(documentListener);
        editorEventMulticaster.removeEditorMouseListener(editorMouseListener);
        editorEventMulticaster.removeEditorMouseMotionListener(editorMouseMotionListener);
        editorEventMulticaster.removeCaretListener(caretListener);
        editorEventMulticaster.removeSelectionListener(selectionListener);
        editorEventMulticaster.removeVisibleAreaListener(visibleAreaListener);
        String filePath = dataOutputPath + "/ide_tracking.xml";
        XMLWriter.writeToXML(iDETracking, filePath);
    }

    public void pauseTracking() {
        isTracking = false;
    }

    public void resumeTracking() {
        isTracking = true;
    }

    @Override
    public void dispose() {
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void logFile(String path, String timestamp, String remark, String text) {
        File srcFile = new File(path);
        File destFile = new File(dataOutputPath + "/logs/" + timestamp + ".log");
        String[] codeExtensions = {".java", ".cpp", ".c", ".py", ".rb", ".js", ".md"};
        try {
            if (path.equals("unknown")) {
                FileUtils.writeStringToFile(destFile, text, "UTF-8", true);
            } else {
                if (Arrays.stream(codeExtensions).anyMatch(path::endsWith)) {
                    if (text == null) {
                        FileUtils.copyFile(srcFile, destFile);
                    } else {
                        FileUtils.writeStringToFile(destFile, text, "UTF-8", true);
                    }
                } else {
                    remark += " | NotCodeFile | Fail";
                }
            }
        } catch (IOException e) {
            remark += " | IOException | Fail";
        }

        Element log = iDETracking.createElement("log");
        logs.appendChild(log);
        log.setAttribute("id", "fileLog");
        log.setAttribute("timestamp", timestamp);
        log.setAttribute("path", RelativePathGetter.getRelativePath(path, projectPath));
        log.setAttribute("remark", remark);
    }

    public Element getMouseElement(EditorMouseEvent e, String id) {
        Element mouseElement = iDETracking.createElement("mouse");
        mouseElement.setAttribute("id", id);
        mouseElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
        mouseElement.setAttribute("path", virtualFile != null ?
                RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
        MouseEvent mouseEvent = e.getMouseEvent();
        mouseElement.setAttribute("x", String.valueOf(mouseEvent.getXOnScreen()));
        mouseElement.setAttribute("y", String.valueOf(mouseEvent.getYOnScreen()));
        return mouseElement;
    }

    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }

    public boolean isTracking() {
        return isTracking;
    }

}
