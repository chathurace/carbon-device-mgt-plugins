/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.android;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the Android implementation of DeviceManagerService.
 */
public class AndroidDeviceManagementService implements DeviceManagementService {

    private MobileDeviceManagementDAOFactory mobileDeviceManagementDAOFactory;
    private static final Log log = LogFactory.getLog(AndroidDeviceManagementService.class);

    public AndroidDeviceManagementService() {
        mobileDeviceManagementDAOFactory = new AndroidDAOFactory();
    }

    @Override
    public String getProviderType() {
        return DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return new AndroidFeatureManager();
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Android device : " + device.getDeviceIdentifier());
            }
            AndroidDAOFactory.beginTransaction();
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO().addMobileDevice(
                    mobileDevice);
            AndroidDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException mobileDAOEx) {
                String msg = "Error occurred while roll back the device enrol transaction :" + device.toString();
                log.warn(msg, mobileDAOEx);
            }
            String msg = "Error while enrolling the Android device : " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Android device enrollment data");
            }
            AndroidDAOFactory.beginTransaction();
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO()
                    .updateMobileDevice(mobileDevice);
            AndroidDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException mobileDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, mobileDAOEx);
            }
            String msg = "Error while updating the enrollment of the Android device : " +
                    device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Dis-enrolling Android device : " + deviceId);
            }
            AndroidDAOFactory.beginTransaction();
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO()
                    .deleteMobileDevice(deviceId.getId());
            AndroidDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException mobileDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, mobileDAOEx);
            }
            String msg = "Error while removing the Android device : " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean isEnrolled = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking the enrollment of Android device : " + deviceId.getId());
            }
            MobileDevice mobileDevice =
                    mobileDeviceManagementDAOFactory.getMobileDeviceDAO().getMobileDevice(
                            deviceId.getId());
            if (mobileDevice != null) {
                isEnrolled = true;
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while checking the enrollment status of Android device : " +
                    deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return isEnrolled;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the details of Android device : " + deviceId.getId());
            }
            MobileDevice mobileDevice = mobileDeviceManagementDAOFactory.getMobileDeviceDAO().
                    getMobileDevice(deviceId.getId());
            device = MobileDeviceManagementUtil.convertToDevice(mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while fetching the Android device : " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return device;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceIdentifier, String currentUser,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        boolean status;
		Device deviceDB = this.getDevice(deviceIdentifier);
		// This object holds the current persisted device object
		MobileDevice mobileDeviceDB = MobileDeviceManagementUtil.convertToMobileDevice(deviceDB);

		// This object holds the newly received device object from response
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);

		// Updating current object features using newer ones
		mobileDeviceDB.setLatitude(mobileDevice.getLatitude());
		mobileDeviceDB.setLongitude(mobileDevice.getLongitude());
		mobileDeviceDB.setDeviceProperties(mobileDevice.getDeviceProperties());

        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "updating the details of Android device : " + device.getDeviceIdentifier());
            }
            AndroidDAOFactory.beginTransaction();
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO()
                    .updateMobileDevice(mobileDeviceDB);
            AndroidDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException mobileDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, mobileDAOEx);
            }
            String msg =
                    "Error while updating the Android device : " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all Android devices");
            }
            List<MobileDevice> mobileDevices =
                    mobileDeviceManagementDAOFactory.getMobileDeviceDAO().
                            getAllMobileDevices();
            if (mobileDevices != null) {
                devices = new ArrayList<Device>();
                for (MobileDevice mobileDevice : mobileDevices) {
                    devices.add(MobileDeviceManagementUtil.convertToDevice(mobileDevice));
                }
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while fetching all Android devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }

    @Override
    public Application[] getApplications(String s, int i, int i2) throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceIdentifier, Application application,
                                        String s) throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceIdentifier,
                                       Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplication(Operation operation, List<DeviceIdentifier> deviceIdentifiers)
            throws ApplicationManagementException {

    }

}