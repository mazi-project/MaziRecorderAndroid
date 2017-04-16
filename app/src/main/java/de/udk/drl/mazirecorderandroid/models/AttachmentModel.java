package de.udk.drl.mazirecorderandroid.models;

import java.io.Serializable;

/**
 * Created by lutz on 11/04/17.
 */

public class AttachmentModel implements Serializable {

    public String text;
    public String[] tags;
    public String file;

    public String interviewId = null;


    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof AttachmentModel)
        {
            AttachmentModel model = (AttachmentModel) object;
            return this.text.equals(model.text);
        }
        return false;
    }

}
