package com.fujitsu.gwt.bewebapp.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class NoteEditor implements ClickHandler{

    LeafData lfdata = null;
    private int currentAccessLevel;

    private Label subjLabel;
    private Label bodyLabel;
    private Label visibleLabel;
    private Label editedByLabel;
    private Label effectiveDateLabel;
    private Label pinPositionLabel;
    private Label choiceLable;

    private TextBox subjText;
    private TextBox pinPos;
    private TextBox choices;
    private DateBox effectiveDate;

    private Button editCancelBtn;
    private Button editSaveAsDraftBtn;
    private Button editSaveBtn;

    private RadioButton optionPublic;
    private RadioButton optionMember;
    private RadioButton optionEditedByYou;
    private RadioButton optionEditedByMember;

    private HorizontalPanel editBarPanel;
    private HorizontalPanel editSubjPanel;
    private HorizontalPanel editVisibility;
    private HorizontalPanel editBody;
    private HorizontalPanel editByPanel;
    private HorizontalPanel effectiveDatePanel;
    private HorizontalPanel pinPositionPanel;
    private HorizontalPanel choicePanel;

    private TinyMCE tinyMCE;
    VerticalPanel dialogEPanel;

    private boolean isNew = false;

    public NoteEditor(LeafData lfdata, VerticalPanel mainPanel) {
        this.dialogEPanel = mainPanel;
        this.lfdata = lfdata;
        this.currentAccessLevel = lfdata.getVisibility();
    }

    public void initEditPanel(){
        subjLabel = new Label("Subject:");
        subjLabel.setStyleName("gridTableColummHeader_3");
        subjText = new TextBox();
        subjText.setStyleName("inputGeneralBig");

        editSubjPanel = new HorizontalPanel();
        //editSubjPanel.setWidth("100%");
        editSubjPanel.add(subjLabel);
        editSubjPanel.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        editSubjPanel.add(subjText);
        //editSubjPanel.setCellWidth(subjText, "60%");
        editSubjPanel.add(new HTML("&nbsp;&nbsp;"));

        editCancelBtn = new Button("Close");
        editCancelBtn.addClickHandler(this);
        editCancelBtn.setStyleName("inputBtn");
        editSaveAsDraftBtn = new Button();
        if(lfdata.isDraft()){
            editSaveAsDraftBtn.setText("Save and Publish");
            editSaveAsDraftBtn.setVisible(true);
        }else{
            editSaveAsDraftBtn.setVisible(false);
        }
        editSaveAsDraftBtn.addClickHandler(this);
        editSaveAsDraftBtn.setStyleName("inputBtn");
        editSaveBtn = new Button("Save");
        editSaveBtn.setStyleName("inputBtn");
        editSaveBtn.addClickHandler(this);
        editBarPanel = new HorizontalPanel();
        editBarPanel.add(editSaveBtn);
        editBarPanel.add(new HTML("&nbsp"));
        editBarPanel.add(editCancelBtn);
        editBarPanel.add(new HTML("&nbsp"));
        editBarPanel.add(editSaveAsDraftBtn);

        visibleLabel = new Label("Visible To:");
        visibleLabel.setStyleName("gridTableColummHeader_3");
        optionPublic = new RadioButton("LfAccess", "Public");
        optionMember = new RadioButton("LfAccess", "Member");

        editVisibility = new HorizontalPanel();
        editVisibility.add(visibleLabel);
        editVisibility.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        editVisibility.add(optionPublic);
        editVisibility.add(new HTML("&nbsp;"));
        editVisibility.add(optionMember);


        if(currentAccessLevel == 1){
            optionPublic.setValue(true);
        }else if(currentAccessLevel == 2){
            optionMember.setValue(true);
        }

        editedByLabel = new Label("Edited By:");
        editedByLabel.setStyleName("gridTableColummHeader_3");
        optionEditedByYou = new RadioButton("LeditedBy", "Only You");
        optionEditedByMember =  new RadioButton("LeditedBy", "Any Project Member ");
        int editByValue = lfdata.getEditedBy();
        if(editByValue == 2){
            optionEditedByMember.setValue(true);
        }
        else if(editByValue == 1){
            optionEditedByYou.setValue(true);
        }
        else{
            throw new RuntimeException("Internal Logic Error: The editedby value HAS NOT BEEN SET!");
        }

        editByPanel = new HorizontalPanel();
        editByPanel.add(editedByLabel);
        editByPanel.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        editByPanel.add(optionEditedByYou);
        editByPanel.add(new HTML("&nbsp"));
        editByPanel.add(optionEditedByMember);

        effectiveDateLabel = new Label("Effective Date:");
        effectiveDateLabel.setStyleName("gridTableColummHeader_3");

        DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
        effectiveDate = new DateBox();
        effectiveDate.setValue(new Date(lfdata.getEffectiveDate()));
        effectiveDate.setFormat(new DateBox.DefaultFormat(dateFormat));
        effectiveDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange(ValueChangeEvent<Date> event) {
                lfdata.setEffectiveDate(effectiveDate.getValue().getTime());
            }
        });

        effectiveDatePanel = new HorizontalPanel();
        effectiveDatePanel.add(effectiveDateLabel);
        effectiveDatePanel.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        effectiveDatePanel.add(effectiveDate);


        pinPositionLabel = new Label("Pin Position:");
        pinPositionLabel.setStyleName("gridTableColummHeader_3");
        pinPos = new TextBox();
        pinPos.setText(lfdata.getPinPosition());
        pinPos.setStyleName("inputGeneralSmall");

        pinPositionPanel = new HorizontalPanel();
        pinPositionPanel.add(pinPositionLabel);
        pinPositionPanel.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        pinPositionPanel.add(pinPos);

        choiceLable = new Label("Choices:");
        choiceLable.setStyleName("gridTableColummHeader_3");
        choices = new TextBox();
        choices.setText(lfdata.getChoice());
        choices.setStyleName("inputGeneralUrl");

        choicePanel = new HorizontalPanel();
        choicePanel.add(choiceLable);
        choicePanel.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        choicePanel.add(choices);

        dialogEPanel.setSpacing(5);
        //dialogEPanel.setBorderWidth(3);
        dialogEPanel.addStyleName("dialogVPanel");
        dialogEPanel.setTitle("Note Editor");
        dialogEPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogEPanel.add(editBarPanel);
        dialogEPanel.add(new HTML("<br><br>"));
        dialogEPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
        dialogEPanel.add(editSubjPanel);
        //dialogEPanel.add(editSubjPanel);

        bodyLabel = new Label("Body:");
        bodyLabel.setStyleName("gridTableColummHeader_3");

        subjText.setText(lfdata.getSubject());
        tinyMCE  = new TinyMCE(30,30,lfdata.getData());
        dialogEPanel.add(new HTML("<br>"));
        editBody = new HorizontalPanel();
        editBody.add(bodyLabel);
        editBody.add(new HTML("&nbsp;&nbsp;&nbsp;"));
        editBody.add(tinyMCE);

        dialogEPanel.add(editBody);
        dialogEPanel.add(new HTML("<br>"));
        dialogEPanel.add(editVisibility);
        dialogEPanel.add(new HTML("<br>"));
        dialogEPanel.add(editByPanel);
        dialogEPanel.add(new HTML("<br>"));
        dialogEPanel.add(effectiveDatePanel);
        dialogEPanel.add(new HTML("<br>"));
        dialogEPanel.add(pinPositionPanel);
        dialogEPanel.add(new HTML("<br>"));
        dialogEPanel.add(choicePanel);

    }

    public void initCreatePanel(){
        isNew = true;
        editSaveAsDraftBtn.setText("Save as Draft");
        editSaveAsDraftBtn.setVisible(true);
        //optionPublic.setValue(true);
    }


    public void onClick(ClickEvent event) {
        Widget sender = (Widget) event.getSource();
        if (sender == editCancelBtn) {
            closeBrowser();
        } else if (sender == editSaveAsDraftBtn) {
            LeafData tld = getEditorLd();
            if("Save as Draft".equals(editSaveAsDraftBtn.getText())){
                tld.setIsDraft(true);
            }else{
                tld.setIsDraft(false);
            }
            if(isNew){
                this.createLeafData(tld);
            }else{
                this.saveLeafData(tld);
            }
        } else if (sender == editSaveBtn) {
            LeafData tld = getEditorLd();
            if(isNew){
                this.createLeafData(tld);
            }else{
                this.saveLeafData(tld);
            }
        }

    }

    private void createLeafData(LeafData tld){
        BeWebApp.leafService.createNote(tld.getPageId(), tld, new AsyncCallback<LeafData>() {
            public void onFailure(Throwable caught) {
                Window.alert("Exception:" + caught);
            }
            public void onSuccess(LeafData rld) {
                lfdata = rld;
                isNew = false;
                if(lfdata.isDraft()){
                    editSaveAsDraftBtn.setText("Save and Publish");
                    editSaveAsDraftBtn.setVisible(true);
                }else{
                    editSaveAsDraftBtn.setVisible(false);
                }
                tinyMCE.setText(rld.getData());

            }
         });
    }


    private LeafData getEditorLd(){
        LeafData tmpLeaf = new LeafData();
        tmpLeaf.setSubject(subjText.getText());
        String txt = tinyMCE.getText();
        tmpLeaf.setData(txt);
        if(optionPublic.getValue()){
            tmpLeaf.setVisibility(1);
        }else if(this.optionMember.getValue()){
            tmpLeaf.setVisibility(2);
        }

        if(optionEditedByYou.getValue()){
            tmpLeaf.setEditedBy(1);
        }else if(this.optionEditedByMember.getValue()){
            tmpLeaf.setEditedBy(2);
        }
        tmpLeaf.setPinPosition(pinPos.getText());
        tmpLeaf.setChoice(choices.getText());


        tmpLeaf.setId(lfdata.getId());
        tmpLeaf.setPageId(lfdata.getPageId());

        tmpLeaf.setIsDraft(lfdata.isDraft());

        return tmpLeaf;

    }

    public void submitLeafData(LeafData tld){
        BeWebApp.leafService.saveNote(tld.getPageId(), tld , new AsyncCallback<LeafData>() {
            public void onFailure(Throwable caught) {
                Window.alert("Exception:" + caught);
              }
              public void onSuccess(LeafData rld) {
                  lfdata = rld;
                  if(lfdata.isDraft()){
                      editSaveAsDraftBtn.setText("Save and Publish");
                      editSaveAsDraftBtn.setVisible(true);
                  }else{
                      editSaveAsDraftBtn.setVisible(false);
                  }
                  tinyMCE.setText(lfdata.getData());
            }
          });
    }
    private void saveLeafData(LeafData tld){
        BeWebApp.leafService.saveNote(tld.getPageId(), tld , new AsyncCallback<LeafData>() {
            public void onFailure(Throwable caught) {
                Window.alert("Exception:" + caught);
              }
              public void onSuccess(LeafData rld) {
                  lfdata = rld;
                  if(lfdata.isDraft()){
                      editSaveAsDraftBtn.setText("Save and Publish");
                      editSaveAsDraftBtn.setVisible(true);
                  }else{
                      editSaveAsDraftBtn.setVisible(false);
                  }
                  tinyMCE.setText(lfdata.getData());
            }
          });
    }

     public static native void closeBrowser()
        /*-{
            $wnd.close();
        }-*/;

     /**
         * encodeURIComponent() -
         * Wrapper for the native URL encoding methods
         * @param text - the text to encode
         * @return the encoded text      */

        protected native String jsEscape(String text) /*-{
            return escape(text);
        }-*/;


}
