/*
 * Copyright (C) 2018. Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package com.mkulesh.onpc.iscp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.mkulesh.onpc.R;
import com.mkulesh.onpc.iscp.messages.AlbumNameMsg;
import com.mkulesh.onpc.iscp.messages.ArtistNameMsg;
import com.mkulesh.onpc.iscp.messages.AudioMutingMsg;
import com.mkulesh.onpc.iscp.messages.AutoPowerMsg;
import com.mkulesh.onpc.iscp.messages.CustomPopupMsg;
import com.mkulesh.onpc.iscp.messages.DigitalFilterMsg;
import com.mkulesh.onpc.iscp.messages.DimmerLevelMsg;
import com.mkulesh.onpc.iscp.messages.FileFormatMsg;
import com.mkulesh.onpc.iscp.messages.FirmwareUpdateMsg;
import com.mkulesh.onpc.iscp.messages.FriendlyNameMsg;
import com.mkulesh.onpc.iscp.messages.GoogleCastAnalyticsMsg;
import com.mkulesh.onpc.iscp.messages.GoogleCastVersionMsg;
import com.mkulesh.onpc.iscp.messages.HdmiCecMsg;
import com.mkulesh.onpc.iscp.messages.InputSelectorMsg;
import com.mkulesh.onpc.iscp.messages.JacketArtMsg;
import com.mkulesh.onpc.iscp.messages.ListInfoMsg;
import com.mkulesh.onpc.iscp.messages.ListTitleInfoMsg;
import com.mkulesh.onpc.iscp.messages.ListeningModeMsg;
import com.mkulesh.onpc.iscp.messages.MasterVolumeMsg;
import com.mkulesh.onpc.iscp.messages.MenuStatusMsg;
import com.mkulesh.onpc.iscp.messages.NetworkServiceMsg;
import com.mkulesh.onpc.iscp.messages.PlayStatusMsg;
import com.mkulesh.onpc.iscp.messages.PowerStatusMsg;
import com.mkulesh.onpc.iscp.messages.PresetCommandMsg;
import com.mkulesh.onpc.iscp.messages.PrivacyPolicyStatusMsg;
import com.mkulesh.onpc.iscp.messages.ReceiverInformationMsg;
import com.mkulesh.onpc.iscp.messages.ServiceType;
import com.mkulesh.onpc.iscp.messages.SpeakerACommandMsg;
import com.mkulesh.onpc.iscp.messages.SpeakerBCommandMsg;
import com.mkulesh.onpc.iscp.messages.TimeInfoMsg;
import com.mkulesh.onpc.iscp.messages.TitleNameMsg;
import com.mkulesh.onpc.iscp.messages.TrackInfoMsg;
import com.mkulesh.onpc.iscp.messages.TuningCommandMsg;
import com.mkulesh.onpc.iscp.messages.XmlListInfoMsg;
import com.mkulesh.onpc.iscp.messages.XmlListItemMsg;
import com.mkulesh.onpc.utils.Logging;
import com.mkulesh.onpc.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State
{
    // Changes
    public enum ChangeType
    {
        NONE,
        COMMON,
        TIME_SEEK,
        MEDIA_ITEMS,
        RECEIVER_INFO
    }

    // Receiver Information
    public String receiverInformation = "";
    String friendlyName = "";
    public Map<String, String> deviceProperties = new HashMap<>();
    public HashMap<String, String> networkServices = new HashMap<>();
    private final int activeZone;
    public List<ReceiverInformationMsg.Zone> zones = new ArrayList<>();
    public final List<ReceiverInformationMsg.Selector> deviceSelectors = new ArrayList<>();

    //Common
    PowerStatusMsg.PowerStatus powerStatus = PowerStatusMsg.PowerStatus.STB;
    public FirmwareUpdateMsg.Status firmwareStatus = FirmwareUpdateMsg.Status.NONE;
    public InputSelectorMsg.InputType inputType = InputSelectorMsg.InputType.NONE;
    public DimmerLevelMsg.Level dimmerLevel = DimmerLevelMsg.Level.NONE;
    public DigitalFilterMsg.Filter digitalFilter = DigitalFilterMsg.Filter.NONE;
    public AudioMutingMsg.Status audioMuting = AudioMutingMsg.Status.NONE;
    public ListeningModeMsg.Mode listeningMode = ListeningModeMsg.Mode.MODE_FF;
    public int volumeLevel = MasterVolumeMsg.NO_LEVEL;
    public AutoPowerMsg.Status autoPower = AutoPowerMsg.Status.NONE;
    public HdmiCecMsg.Status hdmiCec = HdmiCecMsg.Status.NONE;
    public SpeakerACommandMsg.Status speakerA = SpeakerACommandMsg.Status.NONE;
    public SpeakerBCommandMsg.Status speakerB = SpeakerBCommandMsg.Status.NONE;

    // Google cast
    public String googleCastVersion = "N/A";
    public GoogleCastAnalyticsMsg.Status googleCastAnalytics = GoogleCastAnalyticsMsg.Status.NONE;
    private String privacyPolicy = PrivacyPolicyStatusMsg.Status.NONE.getCode();

    // Track info (default values are set in clearTrackInfo method)
    public Bitmap cover;
    public String album, artist, title, currentTime, maxTime, fileFormat;
    Integer currentTrack = null, maxTrack = null;
    private ByteArrayOutputStream coverBuffer = null;

    // Radio
    public List<ReceiverInformationMsg.Preset> presetList = new ArrayList<>();
    public int preset = PresetCommandMsg.NO_PRESET;
    private String frequency = "";

    // Playback
    public PlayStatusMsg.PlayStatus playStatus = PlayStatusMsg.PlayStatus.STOP;
    public PlayStatusMsg.RepeatStatus repeatStatus = PlayStatusMsg.RepeatStatus.OFF;
    public PlayStatusMsg.ShuffleStatus shuffleStatus = PlayStatusMsg.ShuffleStatus.OFF;
    public MenuStatusMsg.TimeSeek timeSeek = MenuStatusMsg.TimeSeek.ENABLE;
    public MenuStatusMsg.TrackMenu trackMenu = MenuStatusMsg.TrackMenu.ENABLE;
    public MenuStatusMsg.Feed positiveFeed = MenuStatusMsg.Feed.DISABLE;
    public MenuStatusMsg.Feed negativeFeed = MenuStatusMsg.Feed.DISABLE;
    public ServiceType serviceIcon = ServiceType.UNKNOWN; // service that is currently playing

    // Navigation
    public ServiceType serviceType = null; // service that is currently selected (may differs from currently playing)
    ListTitleInfoMsg.LayerInfo layerInfo = null;
    private ListTitleInfoMsg.UIType uiType = null;
    int numberOfLayers = 0;
    public int numberOfItems = 0;
    public String titleBar = "";
    private final List<XmlListItemMsg> mediaItems = new ArrayList<>();
    final List<NetworkServiceMsg> serviceItems = new ArrayList<>();
    private final List<String> listInfoItems = new ArrayList<>();

    // Popup
    public CustomPopupMsg popup = null;

    State(int activeZone)
    {
        this.activeZone = activeZone;
        clearTrackInfo();
    }

    @NonNull
    @Override
    public String toString()
    {
        return powerStatus.toString() + "; activeZone=" + activeZone;
    }

    public List<ReceiverInformationMsg.Zone> getZones()
    {
        return zones;
    }

    public int getActiveZone()
    {
        return activeZone;
    }

    public boolean isExtendedZone()
    {
        return activeZone < zones.size() && activeZone != ReceiverInformationMsg.DEFAULT_ACTIVE_ZONE;
    }

    public ReceiverInformationMsg.Zone getActiveZoneInfo()
    {
        return activeZone < zones.size() ? zones.get(activeZone) : null;
    }

    public boolean isOn()
    {
        return powerStatus == PowerStatusMsg.PowerStatus.ON;
    }

    public boolean isPlaying()
    {
        return playStatus != PlayStatusMsg.PlayStatus.STOP;
    }

    public boolean isPlaybackMode()
    {
        return uiType == ListTitleInfoMsg.UIType.PLAYBACK;
    }

    public boolean isMenuMode()
    {
        return uiType == ListTitleInfoMsg.UIType.MENU;
    }

    @NonNull
    public String getModel()
    {
        final String m = deviceProperties.get("model");
        return m == null ? "" : m;
    }

    @NonNull
    public String getDeviceName(boolean useFriendlyName)
    {
        if (useFriendlyName)
        {
            // name from FriendlyNameMsg (NFN)
            if (friendlyName != null && !friendlyName.isEmpty())
            {
                return friendlyName;
            }
            // fallback to ReceiverInformationMsg
            final String name = deviceProperties.get("friendlyname");
            if (name != null)
            {
                return name;
            }
        }
        // fallback to model from ReceiverInformationMsg
        {
            final String name = deviceProperties.get("model");
            if (name != null)
            {
                return name;
            }
        }
        return "";
    }

    private void clearTrackInfo()
    {
        cover = null;
        album = "";
        artist = "";
        title = "";
        fileFormat = "";
        currentTime = TimeInfoMsg.INVALID_TIME;
        maxTime = TimeInfoMsg.INVALID_TIME;
        currentTrack = null;
        maxTrack = null;
    }

    public ChangeType update(ISCPMessage msg)
    {
        if (!(msg instanceof TimeInfoMsg) && !(msg instanceof JacketArtMsg))
        {
            Logging.info(msg, "<< " + msg.toString());
            if (msg.isMultiline())
            {
                msg.logParameters();
            }
        }

        //Common
        if (msg instanceof PowerStatusMsg)
        {
            return isCommonChange(process((PowerStatusMsg) msg));
        }
        if (msg instanceof FirmwareUpdateMsg)
        {
            return isCommonChange(process((FirmwareUpdateMsg) msg));
        }
        if (msg instanceof ReceiverInformationMsg)
        {
            return process((ReceiverInformationMsg) msg) ? ChangeType.RECEIVER_INFO : ChangeType.NONE;
        }
        if (msg instanceof FriendlyNameMsg)
        {
            return isCommonChange(process((FriendlyNameMsg) msg));
        }
        if (msg instanceof DimmerLevelMsg)
        {
            return isCommonChange(process((DimmerLevelMsg) msg));
        }
        if (msg instanceof DigitalFilterMsg)
        {
            return isCommonChange(process((DigitalFilterMsg) msg));
        }
        if (msg instanceof AudioMutingMsg)
        {
            return isCommonChange(process((AudioMutingMsg) msg));
        }
        if (msg instanceof ListeningModeMsg)
        {
            return isCommonChange(process((ListeningModeMsg) msg));
        }
        if (msg instanceof MasterVolumeMsg)
        {
            return isCommonChange(process((MasterVolumeMsg) msg));
        }

        // Common settings
        if (msg instanceof AutoPowerMsg)
        {
            return isCommonChange(process((AutoPowerMsg) msg));
        }
        if (msg instanceof HdmiCecMsg)
        {
            return isCommonChange(process((HdmiCecMsg) msg));
        }
        if (msg instanceof SpeakerACommandMsg)
        {
            return isCommonChange(process((SpeakerACommandMsg) msg));
        }
        if (msg instanceof SpeakerBCommandMsg)
        {
            return isCommonChange(process((SpeakerBCommandMsg) msg));
        }

        // Google cast
        if (msg instanceof GoogleCastVersionMsg)
        {
            return isCommonChange(process((GoogleCastVersionMsg) msg));
        }
        if (msg instanceof GoogleCastAnalyticsMsg)
        {
            return isCommonChange(process((GoogleCastAnalyticsMsg) msg));
        }
        if (msg instanceof PrivacyPolicyStatusMsg)
        {
            return isCommonChange(process((PrivacyPolicyStatusMsg) msg));
        }

        // Track info
        if (msg instanceof JacketArtMsg)
        {
            return isCommonChange(process((JacketArtMsg) msg));
        }
        if (msg instanceof AlbumNameMsg)
        {
            return isCommonChange(process((AlbumNameMsg) msg));
        }
        if (msg instanceof ArtistNameMsg)
        {
            return isCommonChange(process((ArtistNameMsg) msg));
        }
        if (msg instanceof TitleNameMsg)
        {
            return isCommonChange(process((TitleNameMsg) msg));
        }
        if (msg instanceof FileFormatMsg)
        {
            return isCommonChange(process((FileFormatMsg) msg));
        }
        if (msg instanceof TimeInfoMsg)
        {
            return process((TimeInfoMsg) msg) ? ChangeType.TIME_SEEK : ChangeType.NONE;
        }
        if (msg instanceof TrackInfoMsg)
        {
            return isCommonChange(process((TrackInfoMsg) msg));
        }

        // Radio
        if (msg instanceof PresetCommandMsg)
        {
            return isCommonChange(process((PresetCommandMsg) msg));
        }
        if (msg instanceof TuningCommandMsg)
        {
            return isCommonChange(process((TuningCommandMsg) msg));
        }

        // Playback
        if (msg instanceof PlayStatusMsg)
        {
            return isCommonChange(process((PlayStatusMsg) msg));
        }
        if (msg instanceof MenuStatusMsg)
        {
            return isCommonChange(process((MenuStatusMsg) msg));
        }

        // Navigation
        if (msg instanceof CustomPopupMsg)
        {
            return isCommonChange(process((CustomPopupMsg) msg));
        }
        if (msg instanceof InputSelectorMsg)
        {
            return process((InputSelectorMsg) msg) ? ChangeType.MEDIA_ITEMS : ChangeType.NONE;
        }
        if (msg instanceof ListTitleInfoMsg)
        {
            return process((ListTitleInfoMsg) msg) ? ChangeType.MEDIA_ITEMS : ChangeType.NONE;
        }
        if (msg instanceof XmlListInfoMsg)
        {
            return process((XmlListInfoMsg) msg) ? ChangeType.MEDIA_ITEMS : ChangeType.NONE;
        }
        if (msg instanceof ListInfoMsg)
        {
            return process((ListInfoMsg) msg) ? ChangeType.MEDIA_ITEMS : ChangeType.NONE;
        }
        return ChangeType.NONE;
    }

    private ChangeType isCommonChange(boolean change)
    {
        return change ? ChangeType.COMMON : ChangeType.NONE;
    }

    private boolean process(PowerStatusMsg msg)
    {
        final boolean changed = msg.getPowerStatus() != powerStatus;
        powerStatus = msg.getPowerStatus();
        if (changed && !isOn())
        {
            clearItems();
        }
        return changed;
    }

    private boolean process(FirmwareUpdateMsg msg)
    {
        final boolean changed = firmwareStatus != msg.getStatus();
        firmwareStatus = msg.getStatus();
        return changed;
    }

    private boolean process(ReceiverInformationMsg msg)
    {
        try
        {
            msg.parseXml();
            receiverInformation = msg.getData();
            deviceProperties = msg.getDeviceProperties();
            networkServices = msg.getNetworkServices();
            zones = msg.getZones();
            deviceSelectors.clear();
            presetList = msg.getPresetList();
            for (ReceiverInformationMsg.Selector s : msg.getDeviceSelectors())
            {
                if (s.isActiveForZone(activeZone))
                {
                    deviceSelectors.add(s);
                }
            }

            return true;
        }
        catch (Exception e)
        {
            Logging.info(msg, "Can not parse XML: " + e.getLocalizedMessage());
        }
        return false;
    }

    private boolean process(FriendlyNameMsg msg)
    {
        final boolean changed = !friendlyName.equals(msg.getFriendlyName());
        friendlyName = msg.getFriendlyName();
        return changed;
    }

    private boolean process(InputSelectorMsg msg)
    {
        final boolean changed = inputType != msg.getInputType();
        inputType = msg.getInputType();
        serviceIcon = ServiceType.UNKNOWN;
        if (!inputType.isMediaList())
        {
            clearItems();
        }

        if (changed && isRadioInput())
        {
            clearTrackInfo();
            timeSeek = MenuStatusMsg.TimeSeek.DISABLE;
        }

        return changed;
    }

    private boolean process(DimmerLevelMsg msg)
    {
        final boolean changed = dimmerLevel != msg.getLevel();
        dimmerLevel = msg.getLevel();
        return changed;
    }

    private boolean process(DigitalFilterMsg msg)
    {
        final boolean changed = digitalFilter != msg.getFilter();
        digitalFilter = msg.getFilter();
        return changed;
    }

    private boolean process(AudioMutingMsg msg)
    {
        final boolean changed = audioMuting != msg.getStatus();
        audioMuting = msg.getStatus();
        return changed;
    }

    private boolean process(ListeningModeMsg msg)
    {
        final boolean changed = listeningMode != msg.getMode();
        listeningMode = msg.getMode();
        return changed;
    }

    private boolean process(MasterVolumeMsg msg)
    {
        final boolean changed = volumeLevel != msg.getVolumeLevel();
        volumeLevel = msg.getVolumeLevel();
        return changed;
    }

    private boolean process(PresetCommandMsg msg)
    {
        final boolean changed = preset != msg.getPreset();
        preset = msg.getPreset();
        return changed;
    }

    private boolean process(TuningCommandMsg msg)
    {
        final boolean changed = !msg.getFrequency().equals(frequency);
        frequency = msg.getFrequency();
        return changed;
    }

    private boolean process(AutoPowerMsg msg)
    {
        final boolean changed = autoPower != msg.getStatus();
        autoPower = msg.getStatus();
        return changed;
    }

    private boolean process(HdmiCecMsg msg)
    {
        final boolean changed = hdmiCec != msg.getStatus();
        hdmiCec = msg.getStatus();
        return changed;
    }

    private boolean process(SpeakerACommandMsg msg)
    {
        final boolean changed = speakerA != msg.getStatus();
        speakerA = msg.getStatus();
        return changed;
    }

    private boolean process(SpeakerBCommandMsg msg)
    {
        final boolean changed = speakerB != msg.getStatus();
        speakerB = msg.getStatus();
        return changed;
    }

    private boolean process(GoogleCastVersionMsg msg)
    {
        final boolean changed = !msg.getData().equals(googleCastVersion);
        googleCastVersion = msg.getData();
        return changed;
    }

    private boolean process(GoogleCastAnalyticsMsg msg)
    {
        final boolean changed = googleCastAnalytics != msg.getStatus();
        googleCastAnalytics = msg.getStatus();
        return changed;
    }

    private boolean process(PrivacyPolicyStatusMsg msg)
    {
        final boolean changed = !msg.getData().equals(privacyPolicy);
        privacyPolicy = msg.getData();
        return changed;
    }

    private boolean process(JacketArtMsg msg)
    {
        if (msg.getImageType() == JacketArtMsg.ImageType.URL)
        {
            Logging.info(msg, "<< " + msg.toString());
            cover = msg.loadFromUrl();
            return true;
        }
        else if (msg.getRawData() != null)
        {
            final byte in[] = msg.getRawData();
            if (msg.getPacketFlag() == JacketArtMsg.PacketFlag.START)
            {
                Logging.info(msg, "<< " + msg.toString());
                coverBuffer = new ByteArrayOutputStream();
            }
            if (coverBuffer != null)
            {
                coverBuffer.write(in, 0, in.length);
            }
            if (msg.getPacketFlag() == JacketArtMsg.PacketFlag.END)
            {
                Logging.info(msg, "<< " + msg.toString());
                cover = msg.loadFromBuffer(coverBuffer);
                coverBuffer = null;
                return true;
            }
        }
        else
        {
            Logging.info(msg, "<< " + msg.toString());
        }
        return false;
    }

    private boolean process(AlbumNameMsg msg)
    {
        final boolean changed = !msg.getData().equals(album);
        album = msg.getData();
        return changed;
    }

    private boolean process(ArtistNameMsg msg)
    {
        final boolean changed = !msg.getData().equals(artist);
        artist = msg.getData();
        return changed;
    }

    private boolean process(TitleNameMsg msg)
    {
        final boolean changed = !msg.getData().equals(title);
        title = msg.getData();
        return changed;
    }

    private boolean process(TimeInfoMsg msg)
    {
        final boolean changed = !msg.getCurrentTime().equals(currentTime)
                || !msg.getMaxTime().equals(maxTime);
        currentTime = msg.getCurrentTime();
        maxTime = msg.getMaxTime();
        return changed;
    }

    private boolean process(TrackInfoMsg msg)
    {
        final boolean changed = !isEqual(currentTrack, msg.getCurrentTrack())
                || !isEqual(maxTrack, msg.getMaxTrack());
        currentTrack = msg.getCurrentTrack();
        maxTrack = msg.getMaxTrack();
        return changed;
    }

    private boolean isEqual(Integer a, Integer b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return a.equals(b);
    }

    private boolean process(FileFormatMsg msg)
    {
        final boolean changed = !msg.getFullFormat().equals(fileFormat);
        fileFormat = msg.getFullFormat();
        return changed;
    }

    private boolean process(PlayStatusMsg msg)
    {
        final boolean changed = msg.getPlayStatus() != playStatus
                || msg.getRepeatStatus() != repeatStatus
                || msg.getShuffleStatus() != shuffleStatus;
        playStatus = msg.getPlayStatus();
        repeatStatus = msg.getRepeatStatus();
        shuffleStatus = msg.getShuffleStatus();
        return changed;
    }

    private boolean process(MenuStatusMsg msg)
    {
        final boolean changed = timeSeek != msg.getTimeSeek()
                || trackMenu != msg.getTrackMenu()
                || serviceIcon != msg.getServiceIcon()
                || positiveFeed != msg.getPositiveFeed()
                || negativeFeed != msg.getNegativeFeed();
        timeSeek = msg.getTimeSeek();
        trackMenu = msg.getTrackMenu();
        serviceIcon = msg.getServiceIcon();
        positiveFeed = msg.getPositiveFeed();
        negativeFeed = msg.getNegativeFeed();
        return changed;
    }

    private boolean process(ListTitleInfoMsg msg)
    {
        boolean changed = false;
        if (serviceType != msg.getServiceType())
        {
            serviceType = msg.getServiceType();
            clearItems();
            changed = true;
        }
        if (layerInfo != msg.getLayerInfo())
        {
            layerInfo = msg.getLayerInfo();
            changed = true;
        }
        if (uiType != msg.getUiType())
        {
            uiType = msg.getUiType();
            clearItems();
            changed = true;
        }
        if (!titleBar.equals(msg.getTitleBar()))
        {
            titleBar = msg.getTitleBar();
            changed = true;
        }
        if (numberOfLayers != msg.getNumberOfLayers())
        {
            numberOfLayers = msg.getNumberOfLayers();
            changed = true;
        }
        if (numberOfItems != msg.getNumberOfItems())
        {
            numberOfItems = msg.getNumberOfItems();
            changed = true;
        }
        return changed;
    }

    private void clearItems()
    {
        synchronized (mediaItems)
        {
            mediaItems.clear();
        }
        synchronized (serviceItems)
        {
            serviceItems.clear();
        }
    }

    public List<XmlListItemMsg> cloneMediaItems()
    {
        synchronized (mediaItems)
        {
            return new ArrayList<>(mediaItems);
        }
    }

    private boolean process(XmlListInfoMsg msg)
    {
        synchronized (mediaItems)
        {
            if (!inputType.isMediaList())
            {
                mediaItems.clear();
                Logging.info(msg, "skipped: input channel " + inputType.toString() + " is not a media list");
                return true;
            }
            try
            {
                Logging.info(msg, "processing XmlListInfoMsg");
                msg.parseXml(mediaItems, numberOfLayers);
                if (serviceType == ServiceType.PLAYQUEUE &&
                        (currentTrack == null || maxTrack == null))
                {
                    trackInfoFromList(mediaItems);
                }
                return true;
            }
            catch (Exception e)
            {
                mediaItems.clear();
                Logging.info(msg, "Can not parse XML: " + e.getLocalizedMessage());
            }
        }
        return false;
    }

    private void trackInfoFromList(final List<XmlListItemMsg> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            final XmlListItemMsg m = list.get(i);
            if (m.getIcon() == XmlListItemMsg.Icon.PLAY)
            {
                currentTrack = i + 1;
                maxTrack = list.size();
                return;
            }
        }
    }

    public List<NetworkServiceMsg> cloneServiceItems()
    {
        synchronized (serviceItems)
        {
            return new ArrayList<>(serviceItems);
        }
    }

    private boolean process(ListInfoMsg msg)
    {
        if (msg.getInformationType() == ListInfoMsg.InformationType.CURSOR)
        {
            listInfoItems.clear();
            return false;
        }
        if (serviceType == ServiceType.NET)
        {
            // Since the names in ListInfoMsg and ReceiverInformationMsg are
            // not consistent for some services (see https://github.com/mkulesh/onpc/issues/35)
            // we just clone here networkServices provided by ReceiverInformationMsg
            // into serviceItems list by any NET ListInfoMsg
            synchronized (serviceItems)
            {
                serviceItems.clear();
                for (final String code : networkServices.keySet())
                {
                    final ServiceType service =
                            (ServiceType) ISCPMessage.searchParameter(code, ServiceType.values(), ServiceType.UNKNOWN);
                    if (service != ServiceType.UNKNOWN)
                    {
                        serviceItems.add(new NetworkServiceMsg(service));
                    }
                }
            }
            return !serviceItems.isEmpty();
        }
        else if (isUsb())
        {
            final String name = msg.getListedData();
            if (!listInfoItems.contains(name))
            {
                listInfoItems.add(name);
            }
            return false;
        }
        else if (isMenuMode())
        {
            synchronized (mediaItems)
            {
                for (XmlListItemMsg i : mediaItems)
                {
                    if (i.getTitle().toUpperCase().equals(msg.getListedData().toUpperCase()))
                    {
                        return false;
                    }
                }
                final XmlListItemMsg nsMsg = new XmlListItemMsg(
                        msg.getLineInfo(), 0, msg.getListedData(), XmlListItemMsg.Icon.UNKNOWN, true);
                mediaItems.add(nsMsg);
            }
            return true;
        }
        return false;
    }

    private boolean process(CustomPopupMsg msg)
    {
        popup = msg;
        return popup != null;
    }

    public ReceiverInformationMsg.Selector getActualSelector()
    {
        for (ReceiverInformationMsg.Selector s : deviceSelectors)
        {
            if (s.getId().equals(inputType.getCode()))
            {
                return s;
            }
        }
        return null;
    }

    public boolean isRadioInput()
    {
        return inputType == InputSelectorMsg.InputType.FM
                || inputType == InputSelectorMsg.InputType.AM
                || inputType == InputSelectorMsg.InputType.DAB;
    }

    public boolean isUsb()
    {
        return serviceType == ServiceType.USB_FRONT
                || serviceType == ServiceType.USB_REAR;
    }

    public boolean isTopLayer()
    {
        if (!isPlaybackMode())
        {
            if (isRadioInput())
            {
                return true;
            }
            if (serviceType == ServiceType.NET &&
                    layerInfo == ListTitleInfoMsg.LayerInfo.NET_TOP)
            {
                return true;
            }
            if (layerInfo == ListTitleInfoMsg.LayerInfo.SERVICE_TOP)
            {
                return isUsb() || serviceType == ServiceType.UNKNOWN;
            }
        }
        return false;
    }

    public boolean isMediaEmpty()
    {
        return mediaItems.isEmpty() && serviceItems.isEmpty();
    }

    boolean listInfoConsistent()
    {
        if (numberOfItems == 0 || numberOfLayers == 0 || listInfoItems.isEmpty())
        {
            return true;
        }
        synchronized (mediaItems)
        {
            for (String s : listInfoItems)
            {
                for (XmlListItemMsg i : mediaItems)
                {
                    if (i.getTitle().toUpperCase().equals(s.toUpperCase()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    public static String getVolumeLevelStr(int volumeLevel, ReceiverInformationMsg.Zone zone)
    {
        if (zone != null && zone.getVolumeStep() == 0)
        {
            final float f = (float) volumeLevel / 2.0f;
            final DecimalFormat df = Utils.getDecimalFormat("0.0");
            return df.format(f);
        }
        else
        {
            return Integer.toString(volumeLevel, 10);
        }
    }

    public ReceiverInformationMsg.Preset searchPreset()
    {
        for (ReceiverInformationMsg.Preset p : presetList)
        {
            if (p.getId() == preset)
            {
                return p;
            }
        }
        return null;
    }

    public String getTrackInfo(final Context context)
    {
        final StringBuilder str = new StringBuilder();
        final String dashedString = context.getString(R.string.dashed_string);
        if (isRadioInput())
        {
            str.append(preset != PresetCommandMsg.NO_PRESET ? Integer.toString(preset) : dashedString);
            str.append("/");
            str.append(!presetList.isEmpty() ? Integer.toString(presetList.size()) : dashedString);
        }
        else
        {
            str.append(currentTrack != null ? Integer.toString(currentTrack) : dashedString);
            str.append("/");
            str.append(maxTrack != null ? Integer.toString(maxTrack) : dashedString);
        }
        return str.toString();
    }

    @NonNull
    public String getFrequencyInfo(final Context context)
    {
        final String dashedString = context.getString(R.string.dashed_string);
        if (frequency == null)
        {
            return dashedString;
        }
        if (inputType != InputSelectorMsg.InputType.FM)
        {
            return frequency;
        }
        try
        {
            final float f = (float) Integer.parseInt(frequency) / 100.0f;
            final DecimalFormat df = Utils.getDecimalFormat("0.00 MHz");
            return df.format(f);
        }
        catch (Exception e)
        {
            return dashedString;
        }
    }
}
