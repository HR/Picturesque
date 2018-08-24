module Fastlane
  module Actions
    module SharedValues
      BUMPED_VERSION = :BUMPED_VERSION
    end

    class BumpVersionAction < Action
      def self.run(params)
        # fastlane will take care of reading in the parameter and fetching the environment variable:
        # UI.message "BumpVersionAction, version file: #{params[:version_file]}"
        # UI.message "BumpVersionAction, version regex: #{params[:version_regex]}"
        # UI.message "BumpVersionAction, version increment: #{params[:version_increment]}"

        # Regex for version number matching
        version_regex = /[0-9.]+/
        bumped_version = 0

        text = File.readlines(params[:version_file])
        match = text.select { |line| line =~ params[:version_regex] }

        # Raise exception if no version statement match found
        raise 'BumpVersionAction, Could not bump version: No version statement found!' unless match.any?

        # Select the first match
        match = match.first
        UI.message 'BumpVersionAction, Found a version statement: ' + match
        # Replace old version with bumped version
        bumped_line = match.gsub!(version_regex) do |version|
          UI.message 'BumpVersionAction, Current version: ' + version
          # Return the version bumped by the Increment
          if params[:version_increment].is_a? Float
            # Do float arithmetic if float increment (round to two significant figures)
            bumped_version =  (version.to_f + params[:version_increment]).round(2)
          else
            # Otherwise, do integer arithmetic
            bumped_version = version.to_i + params[:version_increment]
          end
        end

        # raise exeception if no version number found
        raise 'BumpVersionAction, Could not bump version: version number not found' if bumped_line.nil?

        # Add bumped version to the context
        Actions.lane_context[SharedValues::BUMPED_VERSION] = bumped_version

        # Write the bumped text to file
        File.open(params[:version_file], 'w') { |file| file.write(text.join) }

        UI.success 'BumpVersionAction, successfully bumped version to ' + bumped_version.to_s
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Bumps the App version'
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
        'Bumps the App version'
      end

      def self.available_options
        # Define all options your action supports.
        [
          FastlaneCore::ConfigItem.new(key: :version_file,
                                       env_name: 'FL_BUMP_VERSION_FILE', # The name of the environment variable
                                       description: 'Bump version file', # a short description of this parameter
                                       is_string: true,
                                       verify_block: proc do |value|
                                         UI.user_error!("version_file does not exist at path '#{value}'") unless File.exist?(value)
                                       end),
          FastlaneCore::ConfigItem.new(key: :version_regex,
                                       env_name: 'FL_BUMP_VERSION_REGEX',
                                       description: 'Bump version regex',
                                       is_string: false,
                                       verify_block: proc do |value|
                                         UI.user_error!("version_regex not a valid regular expression") unless value.instance_of? Regexp
                                       end),
          FastlaneCore::ConfigItem.new(key: :version_increment,
                                       env_name: 'FL_BUMP_VERSION_INCREMENT',
                                       description: 'Bump version increment',
                                       is_string: false,
                                       verify_block: proc do |value|
                                         UI.user_error!("version_increment not a valid Numeric") unless value.is_a? Numeric
                                       end)
        ]
      end

      def self.output
        # Define the shared values you are going to provide
        # Example
        [
          ['BUMPED_VERSION', 'New bumped up version']
        ]
      end

      def self.return_value
        # If your method provides a return value, you can describe here what it does
      end

      def self.authors
        # So no one will ever forget your contribution to fastlane :) You are awesome btw!
        ['@HR']
      end

      def self.is_supported?(platform)
        # you can do things like
        #
        #  true
        #
        #  platform == :ios
        #
        #  [:ios, :mac].include?(platform)
        #

        platform == :android
      end
    end
  end
end
